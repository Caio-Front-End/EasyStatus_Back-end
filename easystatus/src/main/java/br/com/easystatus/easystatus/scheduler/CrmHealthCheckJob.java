package br.com.easystatus.easystatus.scheduler;

import br.com.easystatus.easystatus.entity.Crm;
import br.com.easystatus.easystatus.entity.HealthCheck;
import br.com.easystatus.easystatus.repository.CrmRepository;
import br.com.easystatus.easystatus.repository.HealthCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CrmHealthCheckJob {

    private final CrmRepository crmRepository;
    private final HealthCheckRepository healthCheckRepository;
    
    // O RestClient do Spring Boot 3 (substituto moderno do RestTemplate)
    private final RestClient restClient = RestClient.create();

    @Scheduled(fixedRate = 60000)
    public void monitorCrms() {
        List<Crm> activeCrms = crmRepository.findByAtivoTrue();

        for (Crm crm : activeCrms) {
            long startTime = System.currentTimeMillis();
            int statusCode = 0;
            String errorMessage = null;

            try {
                var response = restClient.get()
                        .uri(crm.getUrl())
                        .retrieve()
                        .toBodilessEntity();
                
                statusCode = response.getStatusCode().value();
            } catch (RestClientResponseException e) {
                // Requisição foi e voltou com erro (Ex: 400, 404, 500)
                statusCode = e.getStatusCode().value();
                errorMessage = e.getMessage();
            } catch (ResourceAccessException e) {
                // Timeout, DNS não encontrado, Servidor desligado
                statusCode = 503; 
                errorMessage = e.getMessage();
            } catch (Exception e) {
                // Erros genéricos
                statusCode = 500;
                errorMessage = e.getMessage();
            }

            long endTime = System.currentTimeMillis();
            int latency = (int) (endTime - startTime);

            // Criar e salvar o HealthCheck
            HealthCheck healthCheck = HealthCheck.builder()
                    .crm(crm)
                    .statusCode(statusCode)
                    .latencyMs(latency)
                    .tentativaRetentativa(false) // Retentativa será uma feature à parte
                    .mensagemErro(errorMessage)
                    .dataCriacao(LocalDateTime.now())
                    .build();

            healthCheckRepository.save(healthCheck);

            // Atualizar o status atual no CRM
            crm.setStatus(String.valueOf(statusCode));
            
            // Regra bônus: registrar a primeira falha ou resetar se voltou
            if (statusCode >= 400 && crm.getDataPrimeiraFalha() == null) {
                crm.setDataPrimeiraFalha(LocalDateTime.now());
            } else if (statusCode < 400) {
                crm.setDataPrimeiraFalha(null);
            }
            
            crmRepository.save(crm);
        }
    }
}
