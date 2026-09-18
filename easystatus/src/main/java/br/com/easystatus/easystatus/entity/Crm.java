package br.com.easystatus.easystatus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_crms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "url", nullable = false, length = 255, unique = true)
    private String url;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "data_primeira_falha")
    private LocalDateTime dataPrimeiraFalha;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "name_db", nullable = false, length = 255)
    private String nameDb;

    @Column(name = "login_db", nullable = false, length = 50)
    private String loginDb;

    @Column(name = "password_db", nullable = false, length = 255)
    private String passwordDb;

    @Column(name = "dns", nullable = false, length = 255)
    private String dns;

    @Column(name = "pem_path", nullable = false, length = 255)
    private String pemPath;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "ativo")
    private Boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analista_incidente_id")
    private User analistaIncidente;

}
