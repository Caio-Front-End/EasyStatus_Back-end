package br.com.easystatus.easystatus.dto;

import java.time.LocalDateTime;

public record CrmResponseDTO(
        Integer id,
        String name,
        String url,
        String logoUrl,
        LocalDateTime dataPrimeiraFalha,
        String ip,
        String dns,
        String status,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        Boolean ativo
) {
}
