package com.eventhub.api.service;

import com.eventhub.api.dto.PasswordUpdateRequestDTO;
import com.eventhub.api.dto.UserUpdateRequestDTO;
import com.eventhub.api.exception.BusinessRuleException;
import com.eventhub.api.model.User;
import com.eventhub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User updateProfile(UserUpdateRequestDTO request, User loggedUser) {
        User user = userRepository.findById(loggedUser.getId())
                .orElseThrow(() -> new BusinessRuleException("Usuário não encontrado."));

        if (!user.getEmail().equals(request.email()) && userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessRuleException("E-mail já está em uso por outro usuário.");
        }

        user.setName(request.name());
        user.setEmail(request.email());

        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(PasswordUpdateRequestDTO request, User loggedUser) {
        User user = userRepository.findById(loggedUser.getId())
                .orElseThrow(() -> new BusinessRuleException("Usuário não encontrado."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessRuleException("A senha atual está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
