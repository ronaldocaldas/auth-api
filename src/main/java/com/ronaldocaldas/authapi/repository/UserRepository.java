package com.ronaldocaldas.authapi.repository;

import com.ronaldocaldas.authapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
