package com.finances.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.finances.dashboard.model.User;
import com.finances.dashboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService extends BaseService<User> {

    @Autowired
    private UserRepository repository;

    @Override
    protected JpaRepository<User, Long> getRepository() {
        return repository;
    }

    @Transactional
    public User update(User user) {
        User existingUser = repository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        return repository.save(existingUser);
    }

}
