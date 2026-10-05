package br.com.easystatus.easystatus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrmRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
        String name,

        @NotBlank(message = "A URL é obrigatória")
        @Size(max = 255, message = "A URL deve ter no máximo 255 caracteres")
        @Pattern(
                regexp = "^https?://(([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d+)?(/.*)?$",
                message = "A URL deve ser válida e começar com http:// ou https://"
        )
        String url,

        @NotBlank(message = "O IP é obrigatório")
        @Size(max = 45, message = "O IP deve ter no máximo 45 caracteres")
        String ip,

        @NotBlank(message = "O nome do banco é obrigatório")
        @Size(max = 255, message = "O nome do banco deve ter no máximo 255 caracteres")
        String nameDb,

        @NotBlank(message = "O login do banco é obrigatório")
        @Size(max = 50, message = "O login do banco deve ter no máximo 50 caracteres")
        String loginDb,

        @NotBlank(message = "A senha do banco é obrigatória")
        @Size(max = 255, message = "A senha do banco deve ter no máximo 255 caracteres")
        String passwordDb,

        @Size(max = 255, message = "O DNS deve ter no máximo 255 caracteres")
        String dns,

        @NotBlank(message = "O caminho do PEM é obrigatório")
        @Size(max = 255, message = "O caminho do PEM deve ter no máximo 255 caracteres")
        String pemPath
) {
}
