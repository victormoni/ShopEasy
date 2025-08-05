package com.shopeasy.ecommerce.service;

import com.shopeasy.ecommerce.dto.request.RegisterRequest;
import com.shopeasy.ecommerce.dto.request.UpdateUserRequest;
import com.shopeasy.ecommerce.model.User;
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
