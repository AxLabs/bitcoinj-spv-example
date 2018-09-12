package com.axlabs.bitcoinj.svp.example.config;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.stream.Stream;

@Configuration
public class BitcoinConfig {

    private final static Logger LOG = LoggerFactory.getLogger(BitcoinConfig.class);

    @Value("${com.axlabs.btc.peer-group-seed}")
    private String[] bitcoinNodePeerGroupSeed;

    @Value("${com.axlabs.btc.network}")
    private String bitcoinNetwork;

    @Bean
    public NetworkParameters chainNetworkParameters() {
        return getNetworkParams(bitcoinNetwork);
    }

    @Bean
    public Context bitcoinContext(NetworkParameters chainNetworkParameters) {
        return new Context(chainNetworkParameters);
    }

    @Bean
    public SPVBlockStore blockStore(NetworkParameters chainNetworkParameters)
            throws IOException, BlockStoreException {
        File blockStoreFile = Files.createTempFile("chain", "tmp").toFile();
        blockStoreFile.deleteOnExit();
        if (blockStoreFile.exists()) {
            blockStoreFile.delete();
        }
        return new SPVBlockStore(chainNetworkParameters, blockStoreFile);
    }

    @Bean
    public BlockChain bitcoinBlockchain(SPVBlockStore blockStore,
                                        Context bitcoinContext, NetworkParameters chainNetworkParameters)
            throws IOException, BlockStoreException {

        if (chainNetworkParameters.equals(MainNetParams.get())) {
            InputStream checkPoints = BitcoinConfig.class.getClassLoader().getResourceAsStream("checkpoints.txt");
            CheckpointManager.checkpoint(chainNetworkParameters, checkPoints, blockStore, 1498867200L);
        } else if (chainNetworkParameters.equals(TestNet3Params.get())) {
            InputStream checkPoints = BitcoinConfig.class.getClassLoader().getResourceAsStream("checkpoints-testnet.txt");
            CheckpointManager.checkpoint(chainNetworkParameters, checkPoints, blockStore, 1498867200L);
        }
        return new BlockChain(bitcoinContext, blockStore);
    }

    @Bean
    public PeerGroup peerGroup(BlockChain bitcoinBlockchain, Context bitcoinContext,
                               NetworkParameters chainNetworkParameters) throws UnknownHostException {
        PeerGroup peerGroup = new PeerGroup(bitcoinContext, bitcoinBlockchain);
        // Regtest has no peer-to-peer functionality
        if (chainNetworkParameters.equals(MainNetParams.get())) {
            Stream.of(bitcoinNodePeerGroupSeed)
                    .forEach((peer) -> {
                        try {
                            peerGroup.addAddress(Inet4Address.getByName(peer));
                        } catch (UnknownHostException e) {
                            LOG.error("Not possible to add peer {} to the peer group. Unknown error: {}", peer, e);
                        }
                    });
        } else if (chainNetworkParameters.equals(TestNet3Params.get())) {
            peerGroup.addPeerDiscovery(new DnsDiscovery(chainNetworkParameters));
        }
        return peerGroup;
    }

    public static NetworkParameters getNetworkParams(String bitcoinNet) {
        switch (bitcoinNet) {
            case "regtest":
                return RegTestParams.get();
            case "testnet":
                return TestNet3Params.get();
            case "mainnet":
                return MainNetParams.get();
            default:
                throw new RuntimeException("Available properties are: " +
                        "(regtest|testnet|main)");
        }
    }

}
