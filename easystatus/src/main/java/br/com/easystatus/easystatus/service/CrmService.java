package br.com.easystatus.easystatus.service;

import br.com.easystatus.easystatus.dto.CrmRequestDTO;
import br.com.easystatus.easystatus.dto.CrmResponseDTO;
import br.com.easystatus.easystatus.dto.CrmUpdateRequestDTO;

import java.util.List;

public interface CrmService {
    CrmResponseDTO create(CrmRequestDTO dto);
    CrmResponseDTO findById(Integer id);
    List<CrmResponseDTO> findAll();
    CrmResponseDTO update(Integer id, CrmUpdateRequestDTO dto);
    void delete(Integer id);
}
