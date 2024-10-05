package com.scripledger.resources;

import com.scripledger.models.GiftCardRequest;
import com.scripledger.services.GiftCardService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/giftcard")
public class GiftCardResource {

    @Inject
    GiftCardService giftCardService;

    private static final Logger LOGGER = Logger.getLogger(GiftCardResource.class);

    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createNewGiftCard(GiftCardRequest request) {
        LOGGER.info("Creating new gift card with serial: " + request.getCardSerial());
        return giftCardService.createNewGiftCard(request)
                .onItem().invoke(giftCard -> {})
                .onFailure().invoke(Throwable::printStackTrace);
    }

    @POST
    @Path("/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> activateGiftCard(@QueryParam("cardSerial") String cardSerial,
                                          @QueryParam("giftCardPublicKey") String giftCardPublicKey) {
        LOGGER.info("Activating gift card with serial: " + cardSerial + " or publicKey: " + giftCardPublicKey);
        return giftCardService.activateGiftCard(cardSerial, giftCardPublicKey)
                .onItem().transform(result -> {
                    if (result.getStatus() == Response.Status.OK.getStatusCode()) {
                        LOGGER.info("Gift card successfully activated");
                    } else {
                        LOGGER.error("Gift card activation failed: " + result.getEntity().toString());
                    }
                    return result;
                })
                .onFailure().recoverWithItem(th -> {
                    LOGGER.error("Gift card activation failed: " + th.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Failed to activate gift card: " + th.getMessage()).build();
                });
    }
}
