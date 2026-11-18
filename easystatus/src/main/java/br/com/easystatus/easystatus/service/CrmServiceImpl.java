package br.com.easystatus.easystatus.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.com.easystatus.easystatus.dto.CrmRequestDTO;
import br.com.easystatus.easystatus.dto.CrmResponseDTO;
import br.com.easystatus.easystatus.dto.CrmUpdateRequestDTO;
import br.com.easystatus.easystatus.entity.Crm;
import br.com.easystatus.easystatus.entity.User;
import br.com.easystatus.easystatus.exception.DataConflictException;
import br.com.easystatus.easystatus.repository.CrmRepository;
import br.com.easystatus.easystatus.repository.HealthCheckRepository;
import br.com.easystatus.easystatus.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrmServiceImpl implements CrmService {

    private final CrmRepository crmRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final HealthCheckRepository healthCheckRepository;

    @Override
    public CrmResponseDTO create(CrmRequestDTO dto) {
        if (crmRepository.findByName(dto.name()).isPresent()) {
            throw new DataConflictException("Já existe um CRM cadastrado com o nome: " + dto.name());
        }

        if (crmRepository.findByUrl(dto.url()).isPresent()) {
            throw new DataConflictException("Já existe um CRM cadastrado com a URL: " + dto.url());
        }

        Crm crm = Crm.builder()
                .name(dto.name())
                .url(dto.url())
                .ip(dto.ip())
                .nameDb(dto.nameDb())
                .loginDb(dto.loginDb())
                .passwordDb(dto.passwordDb())
                .dns(dto.dns())
                .pemPath(dto.pemPath())
                .ativo(true)
                .dataCriacao(LocalDateTime.now())
                .build();

        Crm savedCrm = crmRepository.save(crm);

        // Registro de Auditoria
        auditLogService.logAction(
                "CREATE",
                "tb_crms",
                savedCrm.getId(),
                null,
                savedCrm
        );

        return toResponseDTO(savedCrm);
    }

    @Override
    public CrmResponseDTO findById(Integer id) {
        return crmRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));
    }

    @Override
    public List<CrmResponseDTO> findAll() {
        return crmRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CrmResponseDTO update(Integer id, CrmUpdateRequestDTO dto) {
        Crm crmAntigo = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        // Clone/snapshot do estado anterior para auditoria
        Crm snapshotAntigo = cloneCrmForAudit(crmAntigo);

        crmAntigo.setName(dto.name());
        crmAntigo.setUrl(dto.url());
        crmAntigo.setIp(dto.ip());
        crmAntigo.setDns(dto.dns());

        // Atualiza os dados de infraestrutura somente se novos valores forem informados.
        // Caso contrário, mantém os valores já existentes no banco.
        if (dto.nameDb() != null && !dto.nameDb().isBlank()) {
            crmAntigo.setNameDb(dto.nameDb());
        }

        if (dto.loginDb() != null && !dto.loginDb().isBlank()) {
            crmAntigo.setLoginDb(dto.loginDb());
        }

        if (dto.passwordDb() != null && !dto.passwordDb().isBlank()) {
            crmAntigo.setPasswordDb(dto.passwordDb());
        }

        if (dto.pemPath() != null && !dto.pemPath().isBlank()) {
            crmAntigo.setPemPath(dto.pemPath());
        }

        crmAntigo.setDataAtualizacao(LocalDateTime.now());

        Crm crmAtualizado = crmRepository.save(crmAntigo);

        // Registro de Auditoria
        auditLogService.logAction(
                "UPDATE",
                "tb_crms",
                crmAtualizado.getId(),
                snapshotAntigo,
                crmAtualizado
        );

        return toResponseDTO(crmAtualizado);
    }

    @Override
    public void delete(Integer id) {
        Crm crmAntigo = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        Crm snapshotAntigo = cloneCrmForAudit(crmAntigo);

        crmAntigo.setAtivo(false);
        crmAntigo.setDataAtualizacao(LocalDateTime.now());

        Crm crmInativado = crmRepository.save(crmAntigo);

        // Registro de Auditoria
        auditLogService.logAction(
                "DELETE",
                "tb_crms",
                crmInativado.getId(),
                snapshotAntigo,
                null
        );
    }

    @Override
    public void entrarManutencao(Integer id) {
        Crm crm = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        if (!crm.getAtivo()) {
            throw new IllegalStateException(
                    "Não é possível colocar um CRM inativo em manutenção."
            );
        }

        if ("MANUTENCAO".equals(crm.getStatus())) {
            throw new IllegalStateException(
                    "Este CRM já está em manutenção."
            );
        }

        Crm snapshotAntigo = cloneCrmForAudit(crm);

        crm.setStatus("MANUTENCAO");
        crm.setDataPrimeiraFalha(null);
        crm.setAnalistaIncidente(null);
        crm.setDataTomaCiencia(null);
        crm.setDataAtualizacao(LocalDateTime.now());

        Crm crmAtualizado = crmRepository.save(crm);

        auditLogService.logAction(
                "MAINTENANCE",
                "tb_crms",
                crmAtualizado.getId(),
                snapshotAntigo,
                crmAtualizado
        );
    }

    @Override
    public void sairManutencao(Integer id) {
        Crm crm = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        if (!"MANUTENCAO".equals(crm.getStatus())) {
            throw new IllegalStateException(
                    "Este CRM não está em manutenção."
            );
        }

        Crm snapshotAntigo = cloneCrmForAudit(crm);

        crm.setStatus("ONLINE");
        crm.setDataAtualizacao(LocalDateTime.now());

        Crm crmAtualizado = crmRepository.save(crm);

        auditLogService.logAction(
                "EXIT_MAINTENANCE",
                "tb_crms",
                crmAtualizado.getId(),
                snapshotAntigo,
                crmAtualizado
        );
    }

    @Override
    public void tomarCiencia(Integer id) {
        Crm crm = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        if (!"OFFLINE".equals(crm.getStatus())) {
            throw new IllegalStateException(
                    "Só é possível tomar ciência de um CRM que está offline."
            );
        }

        if (crm.getAnalistaIncidente() != null) {
            throw new IllegalStateException(
                    "Este incidente já possui um analista responsável."
            );
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User analista = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário autenticado não encontrado."
                ));

        Crm snapshotAntigo = cloneCrmForAudit(crm);

        crm.setAnalistaIncidente(analista);
        crm.setDataTomaCiencia(LocalDateTime.now());
        crm.setDataAtualizacao(LocalDateTime.now());

        Crm crmAtualizado = crmRepository.save(crm);

        auditLogService.logAction(
                "TAKE_NOTICE",
                "tb_crms",
                crmAtualizado.getId(),
                snapshotAntigo,
                crmAtualizado
        );
    }

    @Override
    public void desfazerCiencia(Integer id) {
        Crm crm = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        if (crm.getAnalistaIncidente() == null) {
            throw new IllegalStateException(
                    "Este incidente ainda não possui um analista responsável."
            );
        }

        Crm snapshotAntigo = cloneCrmForAudit(crm);

        crm.setAnalistaIncidente(null);
        crm.setDataTomaCiencia(null);
        crm.setDataAtualizacao(LocalDateTime.now());

        Crm crmAtualizado = crmRepository.save(crm);

        auditLogService.logAction(
                "UNDO_TAKE_NOTICE",
                "tb_crms",
                crmAtualizado.getId(),
                snapshotAntigo,
                crmAtualizado
        );
    }

    private Crm cloneCrmForAudit(Crm original) {
        return Crm.builder()
                .id(original.getId())
                .name(original.getName())
                .url(original.getUrl())
                .logoUrl(original.getLogoUrl())
                .dataPrimeiraFalha(original.getDataPrimeiraFalha())
                .dataTomaCiencia(original.getDataTomaCiencia())
                .ip(original.getIp())
                .nameDb(original.getNameDb())
                .loginDb(original.getLoginDb())
                .passwordDb(original.getPasswordDb())
                .dns(original.getDns())
                .pemPath(original.getPemPath())
                .status(original.getStatus())
                .dataCriacao(original.getDataCriacao())
                .dataAtualizacao(original.getDataAtualizacao())
                .ativo(original.getAtivo())
                .analistaIncidente(original.getAnalistaIncidente())
                .build();
    }

    private CrmResponseDTO toResponseDTO(Crm crm) {

        Integer analistaIncidenteId = null;
        String analistaIncidenteEmail = null;

        if (crm.getAnalistaIncidente() != null) {
            analistaIncidenteId = crm.getAnalistaIncidente().getId();
            analistaIncidenteEmail = crm.getAnalistaIncidente().getEmail();
        }

        var ultimoHealthCheck = healthCheckRepository
                .findFirstByCrmIdOrderByDataCriacaoDesc(crm.getId());

        Integer ultimoStatusCode = null;
        String ultimaMensagemErro = null;
        Integer ultimaLatencyMs = null;

        if (ultimoHealthCheck != null) {
            ultimoStatusCode = ultimoHealthCheck.getStatusCode();
            ultimaMensagemErro = ultimoHealthCheck.getMensagemErro();
            ultimaLatencyMs = ultimoHealthCheck.getLatencyMs();
        }

        return new CrmResponseDTO(
                crm.getId(),
                crm.getName(),
                crm.getUrl(),
                crm.getLogoUrl(),
                crm.getDataPrimeiraFalha(),
                crm.getDataTomaCiencia(),
                analistaIncidenteId,
                analistaIncidenteEmail,
                ultimoStatusCode,
                ultimaMensagemErro,
                ultimaLatencyMs,
                crm.getIp(),
                crm.getDns(),
                crm.getNameDb(),
                crm.getLoginDb(),
                crm.getPemPath(),
                crm.getStatus(),
                crm.getDataCriacao(),
                crm.getDataAtualizacao(),
                crm.getAtivo()
        );
    }
}
