package br.com.easystatus.easystatus.dto;

import jakarta.validation.constraints.Size;

public record CrmUpdateRequestDTO(
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
        String name,

        @Size(max = 255, message = "A URL deve ter no máximo 255 caracteres")
        String url,

        @Size(max = 45, message = "O IP deve ter no máximo 45 caracteres")
        String ip,

        @Size(max = 255, message = "O nome do banco deve ter no máximo 255 caracteres")
        String nameDb,

        @Size(max = 50, message = "O login do banco deve ter no máximo 50 caracteres")
        String loginDb,

        @Size(max = 255, message = "A senha do banco deve ter no máximo 255 caracteres")
        String passwordDb,

        @Size(max = 255, message = "O DNS deve ter no máximo 255 caracteres")
        String dns,

        @Size(max = 255, message = "O caminho do PEM deve ter no máximo 255 caracteres")
        String pemPath
) {

}
