package at.htlleonding.password;

import at.htlleonding.password.storage.PasswordRepository;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Path("/password")
@Slf4j
public class PasswordResource {
    @Inject
    EntityManager entityManager;

    @Inject
    PasswordRepository passwordRepository;

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Object ping() {
        return new Object() {
            public String message = "pong";
            public int status = 200;
        };
    }
}
