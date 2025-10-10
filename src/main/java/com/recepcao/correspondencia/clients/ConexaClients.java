package com.recepcao.correspondencia.clients;


import com.recepcao.correspondencia.config.ConexaApiConfig;
import com.recepcao.correspondencia.dto.contracts.ContractResponse;
import com.recepcao.correspondencia.dto.contracts.CustomerWithContractResponse;
import com.recepcao.correspondencia.dto.record.PersonBasic;
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
import java.util.function.Function;
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

    public Optional<String> buscarCpfPorPersonId(Long personId) {
        String url = conexaApiConfig.getBaseUrl() + "/person/" + personId;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(conexaApiConfig.getToken());
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<PersonBasic> resp = restTemplate.exchange(
                    url, HttpMethod.GET, entity, PersonBasic.class
            );

            String cpf = Optional.ofNullable(resp.getBody())
                    .map(PersonBasic::cpf)
                    .map(s -> s.replaceAll("\\D+", "")) // normaliza
                    .orElse(null);

            if (cpf != null && isCpfValido(cpf)) return Optional.of(cpf);
            return Optional.empty();

        } catch (HttpClientErrorException | ResourceAccessException e) {
            log.error("Erro ao buscar CPF por personId {}: {}", personId, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar CPF por personId {}: {}", personId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.chars().distinct().count() == 1) return false;
        try {
            int d1 = 0, d2 = 0;
            for (int i = 0; i < 9; i++) {
                int n = cpf.charAt(i) - '0';
                d1 += n * (10 - i);
                d2 += n * (11 - i);
            }
            d1 = (d1 * 10) % 11; d1 = (d1 == 10) ? 0 : d1;
            d2 += d1 * 2; d2 = (d2 * 10) % 11; d2 = (d2 == 10) ? 0 : d2;
            return d1 == (cpf.charAt(9) - '0') && d2 == (cpf.charAt(10) - '0');
        } catch (Exception e) { return false; }
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
