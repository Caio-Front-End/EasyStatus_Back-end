package br.com.easystatus.easystatus.service;

import br.com.easystatus.easystatus.dto.CrmRequestDTO;
import br.com.easystatus.easystatus.dto.CrmResponseDTO;
import br.com.easystatus.easystatus.entity.Crm;
import br.com.easystatus.easystatus.entity.User;
import br.com.easystatus.easystatus.exception.DataConflictException;
import br.com.easystatus.easystatus.repository.CrmRepository;
import br.com.easystatus.easystatus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmServiceImpl implements CrmService {

    private final CrmRepository crmRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Override
    public CrmResponseDTO create(CrmRequestDTO dto) {
        if (crmRepository.findByName(dto.name()).isPresent()) {
            throw new DataConflictException("Já existe um CRM cadastrado com o nome: " + dto.name());
        }
        if (crmRepository.findByUrl(dto.url()).isPresent()) {
            throw new DataConflictException("Já existe um CRM cadastrado com a URL: " + dto.url());
        }

        // Buscar o analista atual para salvar na entidade Crm (opcional mas bom para rastreabilidade de quem cadastrou)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User analista = userRepository.findByEmail(email).orElse(null);

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
                .analistaIncidente(analista)
                .build();

        Crm savedCrm = crmRepository.save(crm);

        // Registro de Auditoria
        auditLogService.logAction("CREATE", "tb_crms", savedCrm.getId(), null, savedCrm);

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
    public CrmResponseDTO update(Integer id, CrmRequestDTO dto) {
        Crm crmAntigo = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        // Clone/snapshot of the old object before modifying its reference since JPA tracks it
        Crm snapshotAntigo = cloneCrmForAudit(crmAntigo);

        crmAntigo.setName(dto.name());
        crmAntigo.setUrl(dto.url());
        crmAntigo.setIp(dto.ip());
        crmAntigo.setNameDb(dto.nameDb());
        crmAntigo.setLoginDb(dto.loginDb());
        crmAntigo.setPasswordDb(dto.passwordDb());
        crmAntigo.setDns(dto.dns());
        crmAntigo.setPemPath(dto.pemPath());
        crmAntigo.setDataAtualizacao(LocalDateTime.now());

        Crm crmAtualizado = crmRepository.save(crmAntigo);

        // Registro de Auditoria (passando o snapshot do antigo e o novo)
        auditLogService.logAction("UPDATE", "tb_crms", crmAtualizado.getId(), snapshotAntigo, crmAtualizado);

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
        auditLogService.logAction("DELETE", "tb_crms", crmInativado.getId(), snapshotAntigo, null);
    }

    private Crm cloneCrmForAudit(Crm original) {
        return Crm.builder()
                .id(original.getId())
                .name(original.getName())
                .url(original.getUrl())
                .logoUrl(original.getLogoUrl())
                .dataPrimeiraFalha(original.getDataPrimeiraFalha())
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
                .build();
    }

    private CrmResponseDTO toResponseDTO(Crm crm) {
        return new CrmResponseDTO(
                crm.getId(),
                crm.getName(),
                crm.getUrl(),
                crm.getLogoUrl(),
                crm.getDataPrimeiraFalha(),
                crm.getIp(),
                crm.getDns(),
                crm.getStatus(),
                crm.getDataCriacao(),
                crm.getDataAtualizacao(),
                crm.getAtivo()
        );
    }
}
