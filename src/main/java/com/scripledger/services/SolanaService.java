package com.scripledger.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.p2p.solanaj.rpc.RpcClient;

@ApplicationScoped
public class SolanaService {

    private static final String SOLANA_RPC_URL = "https://api.devnet.solana.com";
    private final RpcClient client;

    public SolanaService() {
        this.client = new RpcClient(SOLANA_RPC_URL);
    }

}
