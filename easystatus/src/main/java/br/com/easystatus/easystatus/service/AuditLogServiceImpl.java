package br.com.easystatus.easystatus.service;

import br.com.easystatus.easystatus.entity.AuditLog;
import br.com.easystatus.easystatus.entity.User;
import br.com.easystatus.easystatus.repository.AuditLogRepository;
import br.com.easystatus.easystatus.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void logAction(String action, String tableName, Integer recordId, Object oldValue, Object newValue) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return;
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        String oldDataJson = null;
        String newDataJson = null;

        try {
            if (oldValue != null) {
                oldDataJson = objectMapper.writeValueAsString(oldValue);
            }
            if (newValue != null) {
                newDataJson = objectMapper.writeValueAsString(newValue);
            }
        } catch (JsonProcessingException e) {
            oldDataJson = oldValue != null ? "Erro ao serializar para JSON" : null;
            newDataJson = newValue != null ? "Erro ao serializar para JSON" : null;
        }

        String ip = "0.0.0.0";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                ip = attributes.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {
        }

        AuditLog auditLog = AuditLog.builder()
                .acao(action)
                .tabelaAfetada(tableName)
                .registroId(recordId)
                .valorAnterior(oldDataJson)
                .novoValor(newDataJson)
                .ipOrigem(ip)
                .dataCriacao(LocalDateTime.now())
                .user(user)
                .build();

        auditLogRepository.save(auditLog);
    }
}
