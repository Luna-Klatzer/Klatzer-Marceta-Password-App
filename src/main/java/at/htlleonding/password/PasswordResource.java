package at.htlleonding.password;

import at.htlleonding.password.storage.UserRepository;
import at.htlleonding.password.models.*;
import at.htlleonding.password.models.dto.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Path("/password")
@Slf4j
public class PasswordResource {
    @Inject
    EntityManager entityManager;

    @Inject
    UserRepository passwordRepository;

    @ConfigProperty(name = "global.pepper")
    String globalPepper;

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Object ping() {
        return new Object() {
            public String message = "pong";
            public int status = 200;
        };
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object login(LoginDto loginDto) {
        log.info("Login attempt for user {}", loginDto.username);
        User user = entityManager.find(User.class, loginDto.username);
        if (user == null) {
            log.info("User {} not found", loginDto.username);
            return new Object() {
                public String message = "User not found";
                public int status = 404;
            };
        }

        boolean validPasswd = HashTools.passwdMatchesHash(
            loginDto.password,
            user.getPassword(),
            user.getSalt(),
            this.globalPepper
        );
        if (!validPasswd) {
            log.info("Invalid password for user {}", loginDto.username);
            return new Object() {
                public String message = "Invalid password";
                public int status = 401;
            };
        }

        log.info("User {} logged in", loginDto.username);
        return new Object() {
            public String message = "Login successful";
            public int status = 200;
        };
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object register(RegisterDto registerDto) {
        log.info("Registering user {}", registerDto.username);
        User user = entityManager.find(User.class, registerDto.username);
        if (user != null) {
            log.info("User {} already exists", registerDto.username);
            return new Object() {
                public String message = "User already exists";
                public int status = 409;
            };
        }

        passwordRepository.createUser(
            registerDto.username,
            registerDto.phoneNumber,
            registerDto.password
        );
        return new Object() {
            public String message = "User created";
            public int status = 201;
        };
    }

    @GET
    @Path("/user/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getUser(@PathParam("username") String username) {
        log.info("Getting user {}", username);
        User user = entityManager.find(User.class, username);
        if (user == null) {
            log.info("User {} not found", username);
            return new Object() {
                public String message = "User not found";
                public int status = 404;
            };
        }

        return UserDto.fromUser(user);
    }
}
