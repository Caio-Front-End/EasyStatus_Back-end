package br.com.easystatus.easystatus.service;

import br.com.easystatus.easystatus.dto.CrmRequestDTO;
import br.com.easystatus.easystatus.dto.CrmResponseDTO;
import br.com.easystatus.easystatus.entity.Crm;
import br.com.easystatus.easystatus.exception.DataConflictException;
import br.com.easystatus.easystatus.repository.CrmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrmServiceImpl implements CrmService {

    private final CrmRepository crmRepository;

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
        Crm crm = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));

        crm.setName(dto.name());
        crm.setUrl(dto.url());
        crm.setIp(dto.ip());
        crm.setNameDb(dto.nameDb());
        crm.setLoginDb(dto.loginDb());
        crm.setPasswordDb(dto.passwordDb());
        crm.setDns(dto.dns());
        crm.setPemPath(dto.pemPath());
        crm.setDataAtualizacao(LocalDateTime.now());

        return toResponseDTO(crmRepository.save(crm));
    }

    @Override
    public void delete(Integer id) {
        // Implementação inicial via Soft Delete (inativação)
        Crm crm = crmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CRM não encontrado"));
        crm.setAtivo(false);
        crm.setDataAtualizacao(LocalDateTime.now());
        crmRepository.save(crm);
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
