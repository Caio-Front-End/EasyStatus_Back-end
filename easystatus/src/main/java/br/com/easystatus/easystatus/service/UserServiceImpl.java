package br.com.easystatus.easystatus.service;

import br.com.easystatus.easystatus.dto.UserRequestDTO;
import br.com.easystatus.easystatus.dto.UserResponseDTO;
import br.com.easystatus.easystatus.entity.User;
import br.com.easystatus.easystatus.exception.DataConflictException;
import br.com.easystatus.easystatus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new DataConflictException("Já existe um usuário cadastrado com este e-mail.");
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getAtivo(),
                savedUser.getDataCriacao()
        );
    }
}
