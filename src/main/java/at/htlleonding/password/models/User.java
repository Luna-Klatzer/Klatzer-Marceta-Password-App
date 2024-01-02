package at.htlleonding.password.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
public class User {
    @Id
    private String username;

    private String phoneNumber;

    private String password;

    private String salt;
}
