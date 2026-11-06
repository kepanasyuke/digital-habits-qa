package ru.digitalhabits.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.digitalhabits.domain.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);
}
