package com.victormoni.ecommerce.service;

import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.request.UpdateUserRequest;
import com.victormoni.ecommerce.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    User save(User user);

    User create(RegisterRequest dto);

    User update(String username, UpdateUserRequest dto);

    void deleteByUsername(String username);
}
