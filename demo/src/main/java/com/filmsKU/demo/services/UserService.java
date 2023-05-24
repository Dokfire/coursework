package com.filmsKU.demo.services;

import com.filmsKU.demo.models.User;
import com.filmsKU.demo.repos.RoleRepository;
import com.filmsKU.demo.repos.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void addUser(User user) {
        user.setId(null);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setRoles(Set.of(roleRepository.findByName("ROLE_USER")));
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
