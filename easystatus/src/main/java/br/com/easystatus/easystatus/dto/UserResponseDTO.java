package br.com.easystatus.easystatus.dto;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Integer id,
        String name,
        String email,
        Boolean ativo,
        LocalDateTime dataCriacao
) {
}
