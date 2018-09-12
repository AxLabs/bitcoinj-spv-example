package com.axlabs.bitcoinj.svp.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class BitcoinSVPExampleApplication {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BitcoinSVPExampleApplication.class);

    public static void main(String[] args) {
        run(BitcoinSVPExampleApplication.class, args);
    }

}
