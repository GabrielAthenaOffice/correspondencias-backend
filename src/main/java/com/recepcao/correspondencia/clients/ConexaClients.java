package com.recepcao.correspondencia.clients;


import com.fasterxml.jackson.databind.JsonNode;
import com.recepcao.correspondencia.config.ConexaApiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recepcao.correspondencia.dto.responses.ConexaCustomerListResponse;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

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

    // === PUBLIC ===
    public Optional<String> buscarCpfPorCustomerId(Long customerId) {
        // 1) POST persons por customerId
        Optional<String> v = postPersonsAndPickCpf(Map.of("customerId", List.of(customerId)));
        if (v.isPresent()) return v;

        // 2) fallback: POST persons por companyId do customer
        CustomerResponse cust = buscarEmpresaPorId(customerId);
        if (cust != null && cust.getCustomerId() != null) {
            v = postPersonsAndPickCpf(Map.of("companyId", List.of(cust.getCustomerId())));
            if (v.isPresent()) return v;
        }

        // 3) acabou
        return Optional.empty();
    }

    // === PRIVATE ===
    private Optional<String> postPersonsAndPickCpf(Map<String, Object> filtro) {
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(conexaApiConfig.getToken());
            h.setAccept(List.of(MediaType.APPLICATION_JSON));
            h.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> r = restTemplate.exchange(
                    conexaApiConfig.getBaseUrl() + "/persons",
                    HttpMethod.POST,
                    new HttpEntity<>(filtro, h),
                    String.class
            );

            String body = r.getBody();
            if (body == null || body.isBlank()) return Optional.empty();

            // LOG rápido p/ depurar (curto, não vaza dados sensíveis)
            log.debug("persons({}) -> {} chars, init='{}'",
                    filtro.keySet(), body.length(),
                    body.substring(0, Math.min(200, body.length())).replaceAll("\\s+"," ").trim());

            return extrairCpfFlex(body);
        } catch (HttpClientErrorException e) {
            log.warn("POST /persons {} => {} body={}", filtro.keySet(), e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("POST /persons {} falhou: {}", filtro.keySet(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> extrairCpfFlex(String json) {
        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(json);

            // normaliza nó da lista
            JsonNode node = root;
            if (root.has("data")) node = root.get("data");
            if (node.has("items")) node = node.get("items");
            if (node.has("results")) node = node.get("results");
            if (!node.isArray()) {
                // pode vir objeto único
                return pickCpfFromNode(node);
            }

            // varre array e pega o primeiro CPF válido
            for (JsonNode n : node) {
                Optional<String> cpf = pickCpfFromNode(n);
                if (cpf.isPresent()) return cpf;
            }
        } catch (Exception e) {
            log.warn("Parse persons falhou: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<String> pickCpfFromNode(JsonNode n) {
        if (n == null || n.isNull()) return Optional.empty();

        // direto
        String cpf = cleanCpf(n.path("cpf").asText(null));
        if (isCpfValido(cpf)) return Optional.of(cpf);

        // aninhado comum (ex.: { person: { cpf: ... } })
        if (n.has("person")) {
            cpf = cleanCpf(n.get("person").path("cpf").asText(null));
            if (isCpfValido(cpf)) return Optional.of(cpf);
        }

        // às vezes vem dentro de "attributes"/"document"
        if (n.has("attributes")) {
            cpf = cleanCpf(n.get("attributes").path("cpf").asText(null));
            if (isCpfValido(cpf)) return Optional.of(cpf);
            cpf = cleanCpf(n.get("attributes").path("document").asText(null));
            if (isCpfValido(cpf)) return Optional.of(cpf);
        }

        // fallback: tenta "document"
        cpf = cleanCpf(n.path("document").asText(null));
        if (isCpfValido(cpf)) return Optional.of(cpf);

        return Optional.empty();
    }

    private String cleanCpf(String v) { return v == null ? null : v.replaceAll("\\D+",""); }

    private boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.chars().distinct().count()==1) return false;
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
