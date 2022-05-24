package rest.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import rest.data.entities.AppUser;

public interface AppUserRepo extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);
}
