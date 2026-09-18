package br.com.easystatus.easystatus.scheduler;

import br.com.easystatus.easystatus.repository.HealthCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataCleanupJob {

    private final HealthCheckRepository healthCheckRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanOldHealthChecks() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        
        try {
            healthCheckRepository.deleteByDataCriacaoBefore(cutoffDate);
            log.info("Limpeza automática de Health Checks (Data Cleanup) executada com sucesso. Registos anteriores a {} foram apagados de forma permanente.", cutoffDate);
        } catch (Exception e) {
            log.error("Ocorreu um erro ao executar a rotina de limpeza automática do banco de dados: ", e);
        }
    }
}
