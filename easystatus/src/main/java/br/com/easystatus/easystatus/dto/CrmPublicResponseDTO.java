package br.com.easystatus.easystatus.dto;

public record CrmPublicResponseDTO(

        Integer id,

        String name,

        String url,

        String logoUrl,

        String status,

        Boolean ativo,

        Boolean emAtendimento

) {}