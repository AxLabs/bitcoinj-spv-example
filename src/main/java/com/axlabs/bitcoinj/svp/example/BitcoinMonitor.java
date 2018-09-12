package com.axlabs.bitcoinj.svp.example;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
public class BitcoinMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BitcoinMonitor.class);

    private final PeerGroup bitcoinPeerGroup;
    private final BlockChain bitcoinBlockchain;

    @Autowired
    public BitcoinMonitor(BlockChain bitcoinBlockchain,
                          PeerGroup bitcoinPeerGroup) {

        this.bitcoinBlockchain = bitcoinBlockchain;
        this.bitcoinPeerGroup = bitcoinPeerGroup;
    }

    @PostConstruct
    protected void start() {
        bitcoinPeerGroup.start();

        final DownloadProgressTracker downloadListener = new DownloadProgressTracker() {
            @Override
            protected void doneDownload() {
                LOG.info("Download done, now sending block numbers.");
                final int startBlockHeight = bitcoinBlockchain.getBestChainHeight();
                bitcoinPeerGroup.addBlocksDownloadedEventListener((peer, block, filteredBlock, blocksLeft) -> {
                    if (bitcoinBlockchain.getBestChainHeight() > startBlockHeight) {
                        LOG.info("Block: {}", block.toString());
                    }
                });
            }

            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                LOG.info("Downloading chain: {}%", (int) pct);
            }
        };
        bitcoinPeerGroup.startBlockChainDownload(downloadListener);
        LOG.info("Downloading SPV blockchain...");
    }

}
