import at.htlleonding.password.HashTools;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@Slf4j
public class HashToolsTest {
    @Inject
    EntityManager entityManager;

    @Test
    public void byteToHex() {
        byte b = 0x0A;
        String expected = "0a";

        String actual = HashTools.byteToHex(b);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void encodeHexString() {
        byte[] bytes = new byte[] {0x0A, 0x0B};
        String expected = "0a0b";

        String actual = HashTools.encodeHexString(bytes);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void hexToByte() {
        String hexString = "0A";
        byte expected = 0x0A;

        byte actual = HashTools.hexToByte(hexString);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void hexToByteInvalid() {
        String hexString = "0G";

        try {
            HashTools.hexToByte(hexString);
            assertThat(false).isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(true).isTrue();
        }
    }

    @Test
    public void decodeHexString() {
        String hexString = "0A0B";
        byte[] expected = new byte[] {0x0A, 0x0B};

        byte[] actual = HashTools.decodeHexString(hexString);
        assert(expected.length == actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertThat(actual[i]).isEqualTo(expected[i]);
        }
    }

    @Test
    public void generateSalt() {
        String salt = HashTools.generateSalt();
        assertThat(salt.length()).isEqualTo(32);
    }

    @Test
    public void toHash() {
        String pepper = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        String password1 = "password";
        String salt1 = HashTools.generateSalt();

        String password2 = "password";
        String salt2 = HashTools.generateSalt();

        // Salt should distinguish the two hashes
        String hash1 = HashTools.toHash(password1, salt1, pepper);
        String hash2 = HashTools.toHash(password2, salt2, pepper);
        assertThat(hash1).isNotEqualTo(hash2);
    }
}
