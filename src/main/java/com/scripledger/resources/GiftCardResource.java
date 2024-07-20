package com.scripledger.resources;

import com.scripledger.models.GiftCard;
import com.scripledger.models.GiftCardRequest;
import com.scripledger.services.BrandsService;
import com.scripledger.services.GiftCardService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/giftcard")
public class GiftCardResource {

    @Inject
    GiftCardService giftCardService;

    private static final Logger LOGGER = Logger.getLogger(BrandsService.class);
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createGiftCards(List<GiftCardRequest> requests) {
        LOGGER.info("Creating gift cards: " + requests);
        return giftCardService.createNewPhysical(requests)
                .onItem().invoke(giftcards -> LOGGER.info("request fetched: " + giftcards))
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @GET
    @Path("/{cardSerial}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<GiftCard> getGiftCard(@PathParam("cardSerial") String cardSerial) {
        LOGGER.info("Fetching brand with ID: " + cardSerial);
        return giftCardService.getGiftCard(cardSerial)
                .onItem().invoke(brand -> LOGGER.info("cardSerial fetched: " + brand))
                .onFailure().invoke(Throwable::printStackTrace);
    }

}
