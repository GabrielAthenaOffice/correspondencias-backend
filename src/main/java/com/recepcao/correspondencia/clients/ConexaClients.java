package com.recepcao.correspondencia.clients;


import com.fasterxml.jackson.databind.JsonNode;
import com.recepcao.correspondencia.config.ConexaApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.UriComponentsBuilder;
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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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

        String base = conexaApiConfig.getBaseUrl() + "/persons";

        // Somente o array de customerId. Nada de isIndividualCustomer no query.
        URI uri = UriComponentsBuilder.fromHttpUrl(base)
                .query("customerId[]=" + customerId) // mantém "[]"
                .build(true)                          // NÃO re-encode os colchetes
                .toUri();

        log.debug("GET persons URI={}", uri);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            CustomerResponse cust = buscarEmpresaPorId(customerId); // para scoring por nome/email/telefone
            return extrairCpfDaListaDePessoas(resp.getBody(), customerId, cust);
        } catch (HttpClientErrorException e) {
            log.warn("Falha persons por customerId {}: {} body={}", customerId, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        }
    }

    private Optional<String> extrairCpfDaListaDePessoas(String json, Long customerId, CustomerResponse cust) {
        try {
            JsonNode root = new ObjectMapper().readTree(json);
            JsonNode arr = root.has("data") ? root.get("data") : root; // suporta payloads com "data" ou direto
            if (!arr.isArray()) return Optional.empty();

            // 1) candidatos com customerId e CPF válido
            List<JsonNode> candidatos = new ArrayList<>();
            arr.forEach(n -> {
                if (n.path("customerId").asLong(-1) == customerId) {
                    String cpf = limparCpf(n.path("cpf").asText(null));
                    if (isCpfValido(cpf)) candidatos.add(n);
                }
            });
            if (candidatos.isEmpty()) return Optional.empty();

            // 2) prioriza PF titular
            Optional<String> titular = candidatos.stream()
                    .filter(n -> n.path("isIndividualCustomer").asBoolean(false))
                    .map(n -> limparCpf(n.path("cpf").asText(null)))
                    .findFirst();
            if (titular.isPresent()) return titular;

            // 3) senão, sócios
            Optional<String> socio = candidatos.stream()
                    .filter(n -> n.path("isCompanyPartner").asBoolean(false))
                    .map(n -> limparCpf(n.path("cpf").asText(null)))
                    .findFirst();
            if (socio.isPresent()) return socio;

            // 4) senão, aproximação por nome/email/telefone do customer
            String nomeCustomer = cust != null ? normalizar(cust.getName()) : null;
            Set<String> emails = cust != null && cust.getEmailsMessage()!=null
                    ? cust.getEmailsMessage().stream().map(this::normalizar).collect(Collectors.toSet())
                    : Set.of();
            Set<String> phones = cust != null && cust.getPhones()!=null
                    ? cust.getPhones().stream().map(p -> p.replaceAll("\\D+","")).collect(Collectors.toSet())
                    : Set.of();

            return candidatos.stream()
                    .sorted((a,b) -> Integer.compare(score(a, nomeCustomer, emails, phones),
                            score(b, nomeCustomer, emails, phones)))
                    .map(n -> limparCpf(n.path("cpf").asText(null)))
                    .reduce((first, second) -> second); // pega o maior score (último após sort crescente)
        } catch (Exception e) {
            log.warn("Falha ao parsear persons: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private int score(JsonNode n, String nomeCustomer, Set<String> emails, Set<String> phones) {
        int s = 0;
        if (n.path("isCompanyPartner").asBoolean(false)) s += 10;
        if (nomeCustomer != null && normalizar(n.path("name").asText("")).equals(nomeCustomer)) s += 20;

        // match por email
        if (!emails.isEmpty() && n.has("emails") && n.get("emails").isArray()) {
            for (JsonNode e : n.get("emails")) if (emails.contains(normalizar(e.asText()))) { s += 10; break; }
        }
        // match por telefone
        if (!phones.isEmpty()) {
            String cell = n.path("cellNumber").asText("");
            String cellDigits = cell.replaceAll("\\D+","");
            if (!cellDigits.isBlank() && phones.contains(cellDigits)) s += 10;
            if (n.has("phones") && n.get("phones").isArray()) {
                for (JsonNode p : n.get("phones")) {
                    String d = p.asText("").replaceAll("\\D+","");
                    if (phones.contains(d)) { s += 10; break; }
                }
            }
        }
        return s;
    }

    private String limparCpf(String v) { return v == null ? null : v.replaceAll("\\D+",""); }

    private String normalizar(String s) {
        if (s == null) return null;
        String t = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return t.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length()!=11 || cpf.chars().distinct().count()==1) return false;
        int d1=0,d2=0; for (int i=0;i<9;i++){int n=cpf.charAt(i)-'0'; d1+=n*(10-i); d2+=n*(11-i);}
        d1=(d1*10)%11; if (d1==10) d1=0; d2+=d1*2; d2=(d2*10)%11; if (d2==10) d2=0;
        return d1==(cpf.charAt(9)-'0') && d2==(cpf.charAt(10)-'0');
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
