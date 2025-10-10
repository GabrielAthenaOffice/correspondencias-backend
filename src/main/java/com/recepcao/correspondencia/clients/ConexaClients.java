package com.recepcao.correspondencia.clients;


import com.fasterxml.jackson.databind.JsonNode;
import com.recepcao.correspondencia.config.ConexaApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.UriUtils;
import com.recepcao.correspondencia.dto.responses.ConexaCustomerListResponse;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConexaClients {

    private final RestTemplate restTemplate;
    private final ConexaApiConfig conexaApiConfig;

    public List<CustomerResponse> buscarEmpresasPorNome(String nomeEmpresa) {
        String url = conexaApiConfig.getBaseUrl() + "/customers?name=" + nomeEmpresa;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(conexaApiConfig.getToken());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Buscando empresas no Conexa com nome: {}", nomeEmpresa);

            ResponseEntity<ConexaCustomerListResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ConexaCustomerListResponse.class
            );

            if (response.getBody() != null && response.getBody().getData() != null) {
                log.info("Encontradas {} empresas no Conexa", response.getBody().getData().size());
                return response.getBody().getData();
            } else {
                log.warn("Resposta vazia do Conexa para busca: {}", nomeEmpresa);
                return Collections.emptyList();
            }

        } catch (HttpClientErrorException e) {
            log.error("Erro HTTP ao buscar empresas no Conexa: {} - Status: {}", e.getMessage(), e.getStatusCode());
            return Collections.emptyList();
        } catch (ResourceAccessException e) {
            log.error("Erro de conexão ao buscar empresas no Conexa: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar empresas no Conexa: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

// === ConexaClients ===

    public Optional<String> buscarCpfPorCustomerId(Long customerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(conexaApiConfig.getToken());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 1) tentar relação customer -> persons
        try {
            // AJUSTE o path conforme a doc (ex.: /customers/{id}/persons ou /persons?customerId=)
            String urlByCustomer = conexaApiConfig.getBaseUrl() + "/customers/" + customerId + "/persons";
            ResponseEntity<String> r = restTemplate.exchange(urlByCustomer, HttpMethod.GET, entity, String.class);
            Optional<String> cpf = extrairCpfDePayload(r.getBody(), customerId);
            if (cpf.isPresent()) return cpf;
        } catch (Exception e) {
            log.debug("Busca CPF via customer->persons falhou: {}", e.getMessage());
        }

        // 2) fallback por e-mails do customer
        CustomerResponse cust = buscarEmpresaPorId(customerId);
        if (cust != null && cust.getEmailsMessage() != null) {
            for (String email : cust.getEmailsMessage()) {
                try {
                    // AJUSTE o path (ex.: /persons?email=)
                    String urlByEmail = conexaApiConfig.getBaseUrl() + "/persons?email=" +
                            UriUtils.encode(email, StandardCharsets.UTF_8);
                    ResponseEntity<String> r = restTemplate.exchange(urlByEmail, HttpMethod.GET, entity, String.class);
                    Optional<String> cpf = extrairCpfDePayload(r.getBody(), customerId);
                    if (cpf.isPresent()) return cpf;
                } catch (Exception ignored) {}
            }
        }

        // 3) fallback por telefones do customer
        if (cust != null && cust.getPhones() != null) {
            for (String phone : cust.getPhones()) {
                try {
                    String digits = phone.replaceAll("\\D+", "");
                    // AJUSTE o path (ex.: /persons?phone=)
                    String urlByPhone = conexaApiConfig.getBaseUrl() + "/persons?phone=" + digits;
                    ResponseEntity<String> r = restTemplate.exchange(urlByPhone, HttpMethod.GET, entity, String.class);
                    Optional<String> cpf = extrairCpfDePayload(r.getBody(), customerId);
                    if (cpf.isPresent()) return cpf;
                } catch (Exception ignored) {}
            }
        }

        return Optional.empty();
    }

    private Optional<String> extrairCpfDePayload(String json, Long expectedCustomerId) {
        try {
            JsonNode root = new ObjectMapper().readTree(json);
            JsonNode node = root.has("data") ? root.get("data") : root; // suporta payloads com "data" ou direto

            if (node.isObject()) {
                return pickCpf(node, expectedCustomerId);
            } else if (node.isArray()) {
                for (JsonNode n : node) {
                    Optional<String> cpf = pickCpf(n, expectedCustomerId);
                    if (cpf.isPresent()) return cpf;
                }
            }
        } catch (Exception e) {
            log.debug("Falha ao parsear payload de persons: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<String> pickCpf(JsonNode node, Long expectedCustomerId) {
        String cpf = node.path("cpf").asText(null);
        if (cpf != null) {
            cpf = cpf.replaceAll("\\D+", "");
            boolean cidOk = true;
            if (node.has("customerId") && node.get("customerId").isNumber()) {
                cidOk = node.get("customerId").asLong() == expectedCustomerId;
            }
            if (cidOk && cpf.length() == 11 && isCpfValido(cpf)) {
                return Optional.of(cpf);
            }
        }
        return Optional.empty();
    }

    private boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.chars().distinct().count() == 1) return false;
        int d1 = 0, d2 = 0;
        for (int i = 0; i < 9; i++) { int n = cpf.charAt(i) - '0'; d1 += n * (10 - i); d2 += n * (11 - i); }
        d1 = (d1 * 10) % 11; if (d1 == 10) d1 = 0;
        d2 += d1 * 2; d2 = (d2 * 10) % 11; if (d2 == 10) d2 = 0;
        return d1 == (cpf.charAt(9) - '0') && d2 == (cpf.charAt(10) - '0');
    }


    public CustomerResponse buscarEmpresaPorId(Long id) {
        String url = conexaApiConfig.getBaseUrl() + "/customer/" + id;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(conexaApiConfig.getToken());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Buscando empresa no Conexa com ID: {}", id);

            ResponseEntity<CustomerResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    CustomerResponse.class
            );

            if (response.getBody() != null) {
                log.info("Empresa encontrada no Conexa: {}", response.getBody().getName());
                return response.getBody();
            } else {
                log.warn("Empresa não encontrada no Conexa com ID: {}", id);
                return null;
            }

        } catch (HttpClientErrorException e) {
            log.error("Erro HTTP ao buscar empresa no Conexa: {} - Status: {}", e.getMessage(), e.getStatusCode());
            return null;
        } catch (ResourceAccessException e) {
            log.error("Erro de conexão ao buscar empresa no Conexa: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar empresa no Conexa: {}", e.getMessage(), e);
            return null;
        }
    }

}
