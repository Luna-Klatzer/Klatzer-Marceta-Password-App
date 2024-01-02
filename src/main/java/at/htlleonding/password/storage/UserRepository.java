package at.htlleonding.password.storage;

import at.htlleonding.password.HashTools;
import at.htlleonding.password.models.ResetKeys;
import at.htlleonding.password.models.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

    @Transactional
    public void createResetKey(String username, String resetKey) {
        if (entityManager.find(User.class, username) == null) {
            throw new RuntimeException("User does not exist");
        }

        ResetKeys resetKeys = ResetKeys.builder()
            .username(username)
            .resetKey(resetKey)
            .build();
        entityManager.persist(resetKeys);
    }

    @Transactional
    public void resetUserPassword(String username, String newPassword) {
        User user = entityManager.find(User.class, username);
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }

        String hashedPassword = HashTools.toHash(newPassword, user.getSalt(), globalPepper);
        user.setPassword(hashedPassword);
    }

    @Transactional
    public void removeResetKey(String resetKey) {
        ResetKeys resetKeys = entityManager.find(ResetKeys.class, resetKey);
        if (resetKeys == null) {
            throw new RuntimeException("Reset key does not exist");
        }

        entityManager.remove(resetKeys);
    }
}
