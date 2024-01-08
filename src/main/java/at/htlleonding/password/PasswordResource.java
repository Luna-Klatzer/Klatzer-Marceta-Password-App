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

@Path("/app")
@Slf4j
public class PasswordResource {
    @Inject
    EntityManager entityManager;

    @Inject
    UserRepository userRepository;

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
            user.getSalt(),
            this.globalPepper,
            user.getPassword() // Password hash
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

        // Validate username (email)
        if (!Validator.validateUsername(registerDto.username)) {
            log.info("Invalid username {}", registerDto.username);
            return new Object() {
                public String message = "Invalid username";
                public int status = 400;
            };
        }

        // Validate phone number
        if (!Validator.validatePhoneNumber(registerDto.phoneNumber)) {
            log.info("Invalid phone number {}", registerDto.phoneNumber);
            return new Object() {
                public String message = "Invalid phone number";
                public int status = 400;
            };
        }

        // Validate password
        if (!Validator.validatePassword(registerDto.password)) {
            log.info("Invalid password");
            return new Object() {
                public String message = "Invalid password";
                public int status = 400;
            };
        }

        userRepository.createUser(
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

    @POST
    @Path("/reset/request/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object requestResetUserPassword(@PathParam("username") String username) {
        log.info("Resetting password for user {}", username);
        User user = entityManager.find(User.class, username);
        if (user == null) {
            log.info("User {} not found", username);
            return new Object() {
                public String message = "User not found";
                public int status = 404;
            };
        }

        String resetKey = HashTools.generateRandomString(32);
        userRepository.createResetKey(username, resetKey);

        // Send the email
        MailAndSMSSimulator.sendEmail(
            user.getUsername(), // The email address is the username
            "Password reset",
            "Your password reset key is: " + resetKey + "\nPlease click on this link to reset your password: http://localhost:8080/password/reset/confirm/" + resetKey
        );

        return new Object() {
            public String message = "Reset key created. View email for further instructions.";
            public int status = 201;
        };
    }

    @POST
    @Path("/reset/confirm/{resetKey}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object confirmResetUserPassword(@PathParam("resetKey") String resetKey, ResetPasswordDto resetPasswordDto) {
        log.info("Confirming password reset for verification code {}", resetKey);
        ResetKey resetKeys = entityManager.find(ResetKey.class, resetKey);
        if (resetKeys == null) {
            log.info("Verification code {} not found", resetKey);
            return new Object() {
                public String message = "Verification code not found";
                public int status = 404;
            };
        }

        User user = entityManager.find(User.class, resetKeys.getUsername());
        if (user == null) {
            log.info("User {} not found", resetKeys.getUsername());
            return new Object() {
                public String message = "User not found";
                public int status = 404;
            };
        }

        // Validate password
        if (!Validator.validatePassword(resetPasswordDto.newPassword)) {
            log.info("Invalid password");
            return new Object() {
                public String message = "Invalid password";
                public int status = 400;
            };
        }

        // Check if the reset key is still valid
        if (resetKeys.getResetKey().equals(resetKey)) {
            log.info("Reset key {} is valid", resetKey);

            try {
                userRepository.resetUserPassword(user.getUsername(), resetPasswordDto.newPassword);
                userRepository.removeResetKey(resetKey);
            } catch (RuntimeException e) {
                log.error("Error while resetting password", e);
                return new Object() {
                    public String message = "Error while resetting password";
                    public int status = 500;
                };
            }

            return new Object() {
                public String message = "Password reset successful";
                public int status = 200;
            };
        } else {
            log.info("Reset key {} is invalid", resetKey);
            return new Object() {
                public String message = "Reset key is invalid";
                public int status = 401;
            };
        }
    }
}
