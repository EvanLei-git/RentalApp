package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Role;

import gr.hua.dit.rentalapp.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleType roleName);

    default Role updateOrInsert(Role role) {
        Optional<Role> existingRole = findByName(role.getName());
        if (existingRole.isPresent()) {
            return existingRole.get();
        }
        else {
            return save(role);
        }
    }
}
