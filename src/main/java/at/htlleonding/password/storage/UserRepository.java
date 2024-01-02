package at.htlleonding.password.storage;

import at.htlleonding.password.HashTools;
import at.htlleonding.password.models.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

@ApplicationScoped
@Slf4j
public class UserRepository {
    @Inject
    EntityManager entityManager;

    @ConfigProperty(name = "global.pepper")
    String globalPepper;

    public boolean userExists(String username) {
        return entityManager.find(User.class, username) != null;
    }

    @Transactional
    public void createUser(String username, String phoneNumber, String password) {
        if (userExists(username)) {
            throw new RuntimeException("User already exists");
        }

        String salt = HashTools.generateSalt();
        String hashedPassword = HashTools.toHash(password, salt, globalPepper);

        User user = User.builder()
            .username(username)
            .phoneNumber(phoneNumber)
            .password(hashedPassword)
            .salt(salt)
            .build();

        entityManager.persist(user);
    }
}
