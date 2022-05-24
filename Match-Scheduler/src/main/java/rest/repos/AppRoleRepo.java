package rest.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import rest.data.entities.AppRole;
import rest.data.entities.AppUser;

public interface AppRoleRepo extends JpaRepository<AppRole, Long> {
    AppRole findByName(String name);
}
