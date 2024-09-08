package com.scripledger.services;

import com.scripledger.collections.GiftCard;
import com.scripledger.models.GiftCardRequest;
import com.scripledger.repositories.GiftCardRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GiftCardService {

    @Inject
    GiftCardRepository giftCardRepository;

    private static final Logger LOGGER = Logger.getLogger(BrandsService.class);

    public Uni<Response> createNewPhysical(List<GiftCardRequest> requests) {
        LOGGER.info("Service: Persisting gift card: " + requests);
        List<GiftCard> giftCards = new ArrayList<>();

        for (GiftCardRequest request : requests) {
            GiftCard giftCard = new GiftCard();
            giftCard.setCardSerial(request.getCardSerial());
            giftCard.setBrandName(request.getBrandName());
            giftCard.setTokenId(request.getTokenId());
            giftCard.setStatus("active");

            giftCards.add(giftCard);
        }

        return giftCardRepository.persist(giftCards)
                .map(persisted -> Response.ok(persisted).build())
                .onFailure().recoverWithItem(failure -> {
                    LOGGER.error("Failed to create gift cards", failure);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(failure.getMessage()).build();
                });
    }

    public Uni<GiftCard> getGiftCard(String cardSerial) {
        ObjectId objectId = new ObjectId(cardSerial);
        LOGGER.info("Service: Retrieving gift card: " + cardSerial);
        return giftCardRepository.findById(objectId);
    }
}
