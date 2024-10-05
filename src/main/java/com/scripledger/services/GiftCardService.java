package com.scripledger.services;

import com.scripledger.collections.GiftCard;
import com.scripledger.models.GiftCardRequest;
import com.scripledger.models.TransactionResponse;
import com.scripledger.models.TransferRequest;
import com.scripledger.repositories.GiftCardRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.bitcoinj.core.Base58;
import org.jboss.logging.Logger;
import org.p2p.solanaj.core.Account;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class GiftCardService {

    @Inject
    GiftCardRepository giftCardRepository;

    @Inject
    NodeService nodeService;

    @Inject
    SolanaService solanaService;

    private static final Logger LOGGER = Logger.getLogger(GiftCardService.class);

    public Uni<Response> createNewGiftCard(GiftCardRequest request) {

        Account account = new Account();
        String publicKey = account.getPublicKey().toBase58();
        String secretKey = new String(account.getSecretKey());

        String balanceQR = "solana:" + publicKey + "?spl-token=" + request.getTokenId() + "&label=Scrip%20Ledger%20Gift%20Card%20Public%20Key";
        String unlockQR = "solana:" + secretKey + "?spl-token=" + request.getTokenId() + "&label=Scrip%20Ledger%20Gift%20Card%20Unlock%20Key";

        GiftCard giftCard = new GiftCard();
        giftCard.setCardSerial(request.getCardSerial());
        giftCard.setTokenId(request.getTokenId());
        giftCard.setPublicKey(publicKey);
        giftCard.setSecretKey(secretKey);
        giftCard.setPublicQR(balanceQR);
        giftCard.setSecretQR(unlockQR);
        giftCard.setStatus("active");
        giftCard.setTimestamp(Date.from(Instant.now()));

        LOGGER.info("creating new gift card public key: " + publicKey);


        return giftCardRepository.persist(giftCard)
                .map(persisted -> Response.ok(giftCard).build())
                .onFailure().recoverWithItem(failure -> {
                    LOGGER.error("Failed to create gift card", failure);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(failure.getMessage()).build();
                });
    }

    public Uni<Response> activateGiftCard(String cardSerial, String giftCardPublicKey) {
        Uni<GiftCard> giftCardUni;
        if (cardSerial != null) {
            giftCardUni = giftCardRepository.findBySerial(cardSerial);
        } else if (giftCardPublicKey != null) {
            giftCardUni = giftCardRepository.findByPublicKey(giftCardPublicKey);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("cardSerial or giftCardPublicKey must be provided").build());
        }

        return giftCardUni
                .onItem().ifNull().failWith(new WebApplicationException("Gift card not found", Response.Status.NOT_FOUND))
                .flatMap(giftCard -> {
                    String mintPubKey = giftCard.getTokenId();

                    return callJsEndpointForTransaction(giftCard.getPublicKey(), mintPubKey)
                            .map(response -> {
                                giftCard.setStatus("redeemed");
                                return Response.ok("Gift card activated successfully").build();
                            });
                })
                .onFailure().recoverWithItem(failure -> {
                    LOGGER.error("Failed to activate gift card", failure);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(failure.getMessage()).build();
                });
    }


    private Uni<Response> callJsEndpointForTransaction(String giftCardPublicKey, String mintPubKey) {
        LOGGER.info("Initiating transfer transaction for gift card public key: " + giftCardPublicKey);

        Account ownerAccount;
        try {
            ownerAccount = solanaService.getOwnerAccount();
        } catch (IOException e) {
            LOGGER.error("Failed to retrieve owner account: " + e.getMessage());
            return Uni.createFrom().item(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve owner account").build());
        }

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setRecipientPubKey(giftCardPublicKey);
        transferRequest.setBusinessAcctSecretKeyBase58(Base58.encode(ownerAccount.getSecretKey()));
        transferRequest.setMintPubKey(mintPubKey);

        return nodeService.transferFromBusiness(transferRequest)
                .onItem().transform(transactionResponse -> {
                    TransactionResponse response = new TransactionResponse();
                    response.setTransactionHash(transactionResponse.getTransactionHash());
                    return Response.ok(response).build();
                })
                .onFailure().recoverWithItem(th -> {
                    LOGGER.error("Failed to complete the transaction: " + th.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Transaction failed").build();
                });
    }

}
