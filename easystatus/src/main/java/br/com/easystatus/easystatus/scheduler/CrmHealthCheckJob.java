package br.com.easystatus.easystatus.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.easystatus.easystatus.entity.Crm;
import br.com.easystatus.easystatus.entity.HealthCheck;
import br.com.easystatus.easystatus.repository.CrmRepository;
import br.com.easystatus.easystatus.repository.HealthCheckRepository;

@Component
public class CrmHealthCheckJob {

    private final CrmRepository crmRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final RestClient restClient;
    private final Executor crmMonitorExecutor;

    private final long intervaloRetry;
    private final int maxRetries;

    public CrmHealthCheckJob(
            CrmRepository crmRepository,
            HealthCheckRepository healthCheckRepository,
            @Value("${easystatus.monitoramento.timeout}") long timeout,
            @Value("${easystatus.monitoramento.intervalo-retry}") long intervaloRetry,
            @Value("${easystatus.monitoramento.max-retries}") int maxRetries,
            @Qualifier("crmMonitorExecutor") Executor crmMonitorExecutor
    ) {
        this.crmRepository = crmRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.intervaloRetry = intervaloRetry;
        this.maxRetries = maxRetries;
        this.crmMonitorExecutor = crmMonitorExecutor;

        this.restClient = RestClient.builder()
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout((int) timeout);
                    setReadTimeout((int) timeout);
                }})
                .build();
    }

    @Scheduled(fixedRateString = "${easystatus.monitoramento.intervalo}")
    public void monitorCrms() {

        List<Crm> activeCrms = crmRepository.findByAtivoTrue()
                .stream()
                .filter(crm -> !"MANUTENCAO".equals(crm.getStatus()))
                .toList();

        for (Crm crm : activeCrms) {
            crmMonitorExecutor.execute(() -> monitorCrm(crm));
        }
    }

    private void monitorCrm(Crm crm) {

        if ("MANUTENCAO".equals(crm.getStatus())) {
            return;
        }

        Integer crmId = crm.getId();
        String urlMonitorada = crm.getUrl();

        for (int tentativa = 0; tentativa <= maxRetries; tentativa++) {

            boolean retry = tentativa > 0;

            long startTime = System.currentTimeMillis();

            int statusCode = 0;
            String errorMessage = null;
            boolean sucesso = false;

            try {
                var response = restClient.get()
                        .uri(urlMonitorada)
                        .retrieve()
                        .toBodilessEntity();

                statusCode = response.getStatusCode().value();

                if (statusCode < 400 || statusCode == 401 || statusCode == 403) {
                    sucesso = true;
                } else {
                    errorMessage = "HTTP " + statusCode;
                }

            } catch (RestClientResponseException e) {

                statusCode = e.getStatusCode().value();
                errorMessage = gerarMensagemErroHttp(statusCode);

                if (statusCode == 401 || statusCode == 403) {
                    sucesso = true;
                }

            } catch (ResourceAccessException e) {

                statusCode = 503;
                errorMessage = "Serviço indisponível (HTTP 503)";

            } catch (Exception e) {

                statusCode = 500;
                errorMessage = "Erro interno do monitoramento (HTTP 500)";
            }

            long endTime = System.currentTimeMillis();
            int latency = (int) (endTime - startTime);

            Crm crmAtual = crmRepository.findById(crmId).orElse(null);

            if (crmAtual == null || !crmAtual.getAtivo()) {
                return;
            }

            if ("MANUTENCAO".equals(crmAtual.getStatus())) {
                return;
            }

            if (!urlMonitorada.equals(crmAtual.getUrl())) {
                return;
            }

            HealthCheck healthCheck = HealthCheck.builder()
                    .crm(crmAtual)
                    .statusCode(statusCode)
                    .latencyMs(latency)
                    .tentativaRetentativa(retry)
                    .mensagemErro(errorMessage)
                    .dataCriacao(LocalDateTime.now())
                    .build();

            healthCheckRepository.save(healthCheck);

            if (sucesso) {
                crmAtual.setStatus("ONLINE");
                crmAtual.setDataPrimeiraFalha(null);
                crmAtual.setAnalistaIncidente(null);
                crmAtual.setDataTomaCiencia(null);

                crmRepository.save(crmAtual);

                return;
            }

            if (crmAtual.getDataPrimeiraFalha() == null) {
                crmAtual.setDataPrimeiraFalha(LocalDateTime.now());
                crmRepository.save(crmAtual);
            }

            if (tentativa < maxRetries) {
                try {
                    Thread.sleep(intervaloRetry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        Crm crmAtual = crmRepository.findById(crmId).orElse(null);

        if (crmAtual == null || !crmAtual.getAtivo()) {
            return;
        }

        if ("MANUTENCAO".equals(crmAtual.getStatus())) {
            return;
        }

        if (!urlMonitorada.equals(crmAtual.getUrl())) {
            return;
        }

        crmAtual.setStatus("OFFLINE");
        crmRepository.save(crmAtual);
    }

    private String gerarMensagemErroHttp(int statusCode) {

        return switch (statusCode) {

            case 400 -> "Requisição inválida (HTTP 400)";

            case 401 -> "Não autorizado (HTTP 401)";

            case 403 -> "Acesso proibido (HTTP 403)";

            case 404 -> "Recurso não encontrado (HTTP 404)";

            case 408 -> "Tempo limite da requisição excedido (HTTP 408)";

            case 429 -> "Muitas requisições (HTTP 429)";

            case 500 -> "Erro interno do servidor (HTTP 500)";

            case 502 -> "Gateway inválido (HTTP 502)";

            case 503 -> "Serviço indisponível (HTTP 503)";

            case 504 -> "Tempo limite do gateway excedido (HTTP 504)";

            default -> "Erro HTTP " + statusCode;
        };
    }
}