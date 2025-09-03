package com.recepcao.correspondencia.clients;


import com.recepcao.correspondencia.config.ConexaApiConfig;
import com.recepcao.correspondencia.dto.contracts.ContractResponse;
import com.recepcao.correspondencia.dto.contracts.CustomerWithContractResponse;
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
