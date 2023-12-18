package at.htlleonding.password.storage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PasswordRepository {
    @Inject
    EntityManager entityManager;
}
