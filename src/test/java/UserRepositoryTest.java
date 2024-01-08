import at.htlleonding.password.models.ResetKey;
import at.htlleonding.password.models.User;
import at.htlleonding.password.storage.UserRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@Slf4j
public class UserRepositoryTest {
    @Inject
    EntityManager entityManager;

    @Inject
    UserRepository userRepository;

    @TestTransaction
    @Test
    public void userExists() {
        String username = "testuser";
        String phoneNumber = "0123456789";
        String password = "testpassword";

        userRepository.createUser(username, phoneNumber, password);

        boolean actual = userRepository.userExists(username);
        assertThat(actual).isTrue();
    }

    @TestTransaction
    @Test
    public void userExistsNot() {
        String username = "testuser";
        String phoneNumber = "0123456789";
        String password = "testpassword";

        userRepository.createUser(username, phoneNumber, password);

        boolean actual = userRepository.userExists("not" + username);
        assertThat(actual).isFalse();
    }

    @TestTransaction
    @Test
    public void createUser() {
        String username = "testuser";
        String phoneNumber = "0123456789";
        String password = "testpassword";

        userRepository.createUser(username, phoneNumber, password);

        // Manually check in the database
        User user = entityManager.find(User.class, username);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getPassword()).isNotEqualTo(password);
        assertThat(user.getSalt()).isNotNull();
    }

    @TestTransaction
    @Test
    public void createUserAlreadyExists() {
        String username = "testuser";
        String phoneNumber = "0123456789";
        String password = "testpassword";

        userRepository.createUser(username, phoneNumber, password);

        try {
            userRepository.createUser(username, phoneNumber, password);
            assertThat(false).isTrue();
        } catch (RuntimeException e) {
            assertThat(true).isTrue();
        }
    }

    @TestTransaction
    @Test
    public void createResetKey() {
        String username = "testuser";
        String phoneNumber = "0123456789";
        String password = "testpassword";
        String resetKey = "testresetkey";

        userRepository.createUser(username, phoneNumber, password);
        userRepository.createResetKey(username, resetKey);

        // Manually check in the database
        ResetKey resetKeys = entityManager.find(ResetKey.class, resetKey);
        assertThat(resetKeys).isNotNull();
        assertThat(resetKeys.getUsername()).isEqualTo(username);
    }
}
