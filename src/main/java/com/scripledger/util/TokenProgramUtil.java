package com.scripledger.util;

import org.jboss.logging.Logger;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class TokenProgramUtil {
    private static final Logger LOGGER = Logger.getLogger(TokenProgramUtil.class);

    public static final int MINT_LAYOUT_SIZE = 82;

    public static final long LAMPORTSFORRENTEXEMPTION() {
        // Implement this method to return the minimum balance required for rent exemption.
        return 2039280; // Example value, replace with actual calculation
    }
    public static final PublicKey PROGRAM_ID = new PublicKey("TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb");

    public static final PublicKey SYSVAR_RENT_ADDRESS = new PublicKey("SysvarRent111111111111111111111111111111111");

    private static final int MINT_TO_INDEX = 7;

    public static TransactionInstruction initializeMint(
            PublicKey mint,
            int decimals,
            PublicKey mintAuthority,
            PublicKey freezeAuthority
    ) {
        List<AccountMeta> keys = Arrays.asList(
                new AccountMeta(mint, false, true),
                new AccountMeta(SYSVAR_RENT_ADDRESS, false, false)
        );

        byte[] data = new byte[4 + 1 + 32 + 1 + (freezeAuthority != null ? 32 : 0)];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0); // Instruction index for initializeMint
        buffer.put((byte) decimals);
        buffer.put(mintAuthority.toByteArray());
        buffer.put((byte) (freezeAuthority != null ? 1 : 0));

        if (freezeAuthority != null) {
            buffer.put(freezeAuthority.toByteArray());
        }

        return new TransactionInstruction(
                PROGRAM_ID,
                keys,
                data
        );
    }



}
