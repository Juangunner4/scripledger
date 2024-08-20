package com.scripledger.util;

import org.bitcoinj.core.Utils;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.util.List;

public class TokenProgramUtil {
    private static final Logger LOGGER = Logger.getLogger(TokenProgramUtil.class);

    public static final int MINT_LAYOUT_SIZE = 82;

    public static final long LAMPORTSFORRENTEXEMPTION() {
        // Implement this method to return the minimum balance required for rent exemption.
        return 2039280; // Example value, replace with actual calculation
    }

    public static final PublicKey PROGRAM_ID = new PublicKey("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");

    public static TransactionInstruction mintTo(
            PublicKey mintAccount,
            PublicKey destinationAccount,
            PublicKey mintAuthority,
            long amount) {

        List<AccountMeta> keys = List.of(
                new AccountMeta(mintAccount, false, true), // Mint account (writable)
                new AccountMeta(destinationAccount, false, true), // Destination account (writable)
                new AccountMeta(mintAuthority, true, false) // Authority (signer)
        );

        byte[] data = new byte[9];
        data[0] = (byte) 2;  // Assuming 0x02 is the opcode for MINT_TO
        Utils.int64ToByteArrayLE(amount, data, 1);  // Amount to mint

        return new TransactionInstruction(
                PROGRAM_ID, // Token program ID
                keys,
                data
        );
    }
}

