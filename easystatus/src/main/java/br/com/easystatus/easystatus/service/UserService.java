package br.com.easystatus.easystatus.service;

import br.com.easystatus.easystatus.dto.UserRequestDTO;
import br.com.easystatus.easystatus.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO create(UserRequestDTO dto);
}
