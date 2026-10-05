package br.com.easystatus.easystatus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.easystatus.easystatus.dto.CrmPublicResponseDTO;
import br.com.easystatus.easystatus.dto.CrmRequestDTO;
import br.com.easystatus.easystatus.dto.CrmResponseDTO;
import br.com.easystatus.easystatus.dto.CrmUpdateRequestDTO;
import br.com.easystatus.easystatus.service.CrmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crms")
@RequiredArgsConstructor
public class CrmController {

    private final CrmService crmService;

    @PostMapping
    public ResponseEntity<CrmResponseDTO> create(@Valid @RequestBody CrmRequestDTO dto) {
        CrmResponseDTO response = crmService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CrmResponseDTO>> findAll() {
        return ResponseEntity.ok(crmService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrmResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(crmService.findById(id));
    }

    @GetMapping("/public/status")
    public ResponseEntity<List<CrmPublicResponseDTO>> getPublicStatus() {
        List<CrmPublicResponseDTO> response = crmService.findAll()
                .stream()
                .filter(crm -> crm.ativo())
                .map(crm -> new CrmPublicResponseDTO(
                        crm.id(),
                        crm.name(),
                        crm.url(),
                        crm.logoUrl(),
                        crm.status(),
                        crm.ativo(),
                        crm.dataTomaCiencia() != null
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrmResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody CrmUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(crmService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        crmService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/manutencao")
    public ResponseEntity<Void> entrarManutencao(@PathVariable Integer id) {
        crmService.entrarManutencao(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/sair-manutencao")
    public ResponseEntity<Void> sairManutencao(@PathVariable Integer id) {
        crmService.sairManutencao(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toma-ciencia")
    public ResponseEntity<Void> tomarCiencia(@PathVariable Integer id) {
        crmService.tomarCiencia(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/desfazer-ciencia")
    public ResponseEntity<Void> desfazerCiencia(@PathVariable Integer id) {
        crmService.desfazerCiencia(id);
        return ResponseEntity.ok().build();
    }
}