package br.com.easystatus.easystatus.dto;

import java.time.LocalDateTime;

public record CrmResponseDTO(

        Integer id,

        String name,

        String url,

        String logoUrl,

        LocalDateTime dataPrimeiraFalha,

        LocalDateTime dataTomaCiencia,

        Integer analistaIncidenteId,

        String analistaIncidenteEmail,

        Integer ultimoStatusCode,

        String ultimaMensagemErro,

        Integer ultimaLatencyMs,

        String ip,

        String dns,

        String nameDb,

        String loginDb,

        String pemPath,

        String status,

        LocalDateTime dataCriacao,

        LocalDateTime dataAtualizacao,

        Boolean ativo

) {

}
