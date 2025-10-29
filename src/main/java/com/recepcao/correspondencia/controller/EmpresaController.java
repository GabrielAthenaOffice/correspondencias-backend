package com.recepcao.correspondencia.controller;

import com.recepcao.correspondencia.clients.AppConstants;
import com.recepcao.correspondencia.clients.ConexaClients;
import com.recepcao.correspondencia.config.APIExceptions;
import com.recepcao.correspondencia.dto.EmpresaResponse;
import com.recepcao.correspondencia.dto.contracts.CriarEmpresaDTO;
import com.recepcao.correspondencia.dto.contracts.CustomerWithContractResponse;
import com.recepcao.correspondencia.dto.responses.ConexaCustomerListResponse;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.Empresa;
import com.recepcao.correspondencia.feign.AditivoRequestDTO;
import com.recepcao.correspondencia.feign.AditivoResponseDTO;
import com.recepcao.correspondencia.mapper.enums.Situacao;
import com.recepcao.correspondencia.mapper.enums.StatusEmpresa;
import com.recepcao.correspondencia.services.CorrespondenciaService;
import com.recepcao.correspondencia.services.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Slf4j
public class EmpresaController {

    private final EmpresaService empresaService;
    private final ConexaClients conexaClients;
    private final CorrespondenciaService correspondenciaService;

    /**
     * Buscar empresa por idê
     * Exemplo: GET /conexa/buscar-por-id/541
     */
    @GetMapping("/conexa/buscar-por-id/{id}")
    public ResponseEntity<CustomerResponse> buscarPorId(@PathVariable Long id) {
        CustomerResponse empresa = conexaClients.buscarEmpresaPorId(id);
        if (empresa == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(empresa);
    }

    /**
     * Buscar empresas por nome
     * Exemplo: GET /conexa/buscar-por-nome?nome=Athena
     */
    @GetMapping("/conexa/buscar-por-nome")
    public ResponseEntity<List<CustomerResponse>> buscarPorNome(@RequestParam String nome) {
        List<CustomerResponse> empresas = conexaClients.buscarEmpresasPorNome(nome);
        return ResponseEntity.ok(empresas);
    }

    /**
     * Buscar empresas de forma geral através modelo CONEXA
     * Exemplo: GET /conexa/buscar-todos-registros
     */
    @GetMapping("/conexa/buscar-todos-registros")
    public ResponseEntity<ConexaCustomerListResponse> buscarTodasEmpresasModeloConexa(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                                                      @RequestParam(name = "pageSize", defaultValue = "50", required = false) Integer pageSize,
                                                                                      @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
                                                                                      @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        ConexaCustomerListResponse customerResponse = empresaService.listarEmpresasModeloConexa(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(customerResponse, HttpStatus.OK);
    }

    /**
     * Buscar empresa por Idê formato ATHENA
     * Exemplo: GET /athena/buscar-por-id/{idê}
     */
    @GetMapping("/athena/buscar-por-id/{id}")
    public ResponseEntity<Optional<Empresa>> buscarPorIdModeloAthena(@PathVariable Long id) {
        Optional<Empresa> empresa = empresaService.buscarEmpresaPorIdModeloAthena(id);

        if (empresa.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(empresa);
    }

    /**
     * Buscar empresas por nome modelo ATHENA
     * Exemplo: GET /athena/buscar-por-nome?nome=Athena
     */
    @GetMapping("/athena/buscar-por-nome")
    public ResponseEntity<Optional<Empresa>> buscarPorNomeModeloAthena(@RequestParam String nome) {
        Optional<Empresa> empresas = empresaService.buscarEmpresaPorNomeModeloAthena(nome);
        return ResponseEntity.ok(empresas);
    }

    /**
     * Buscar empresas por modelo ATHENA
     * Exemplo: GET /athena/buscar-todos-registros
     */
    @GetMapping("/athena/buscar-todos-registros")
    public ResponseEntity<EmpresaResponse> buscarTodasEmpresasModeloAthena(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                                           @RequestParam(name = "pageSize", defaultValue = "50", required = false) Integer pageSize,
                                                                           @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
                                                                           @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        EmpresaResponse empresaResponse = empresaService.listarEmpresasModeloAthena(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(empresaResponse, HttpStatus.OK);
    }

    /**
     * Alterar empresas por modelo ATHENA
     * Exemplo: GET /athena/alterar-empresa/modelo-athena/232
     */
    @PutMapping("/athena/alterar-empresa/modelo-athena/{id}")
    public ResponseEntity<Empresa> alterarStatusModeloAthena(@PathVariable Long id,
                                 @RequestParam(required = false) StatusEmpresa novoStatus,
                                 @RequestParam(required = false) Situacao novaSituacao,
                                                             @RequestParam(required = false) String novaMensagem) {
    Empresa empresaAtualizada = empresaService.alterarStatusModeloAthena(
        id,
        novoStatus,
        novaSituacao,
        novaMensagem
    );

        return ResponseEntity.ok(empresaAtualizada);

    }

    /**
     * Buscar empresas por nome modelo ATHENA com log
     * Exemplo: GET /athena/buscar-por-nome-athena?nome=Athena
     */
    @GetMapping("/athena/buscar-por-nome-athena")
    public ResponseEntity<Empresa> buscarPorNomeAthena(@RequestParam String nome) {
        System.out.println("DEBUG: Endpoint /athena/buscar-por-nome-athena chamado com nome=" + nome);
        Optional<Empresa> empresa = empresaService.buscarEmpresaPorNomeModeloAthena(nome);
        if (empresa.isEmpty()) {
            System.out.println("DEBUG: Empresa não encontrada com nome=" + nome);
            return ResponseEntity.notFound().build();
        }
        System.out.println("DEBUG: Empresa encontrada: " + empresa.get());
        return ResponseEntity.ok(empresa.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarEmpresa(@PathVariable Long id) {
        try {
            empresaService.deletarEmpresa(id);
            return ResponseEntity.ok("Empresa excluída com sucesso");
        } catch (APIExceptions e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno ao excluir empresa");
        }
    }

    @PutMapping("/{id}/atualizar-campos")
    public ResponseEntity<Empresa> atualizarCampos(
            @PathVariable Long id,
            @RequestBody Empresa updates
    ) {
        try {
            Empresa atualizada = empresaService.atualizarCamposEspecificos(id, updates);
            return ResponseEntity.ok(atualizada);
        } catch (APIExceptions e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/criar-por-nome")
    public ResponseEntity<?> criarPorNome(@Valid @RequestBody CriarEmpresaDTO body,
                                          UriComponentsBuilder uriBuilder) {
        try {
            Empresa criada = empresaService.criarPorNome(body);
            var uri = uriBuilder.path("/api/empresas/{id}").buildAndExpand(criada.getId()).toUri();
            return ResponseEntity.created(uri).body(criada); // 201 Created
        } catch (APIExceptions e) {
            String msg = e.getMessage() == null ? "Erro de negócio" : e.getMessage();
            if (msg.toLowerCase().contains("já cadastrada")) {
                return ResponseEntity.status(409).body(msg);   // 409 Conflict
            }
            return ResponseEntity.status(404).body(msg);      // 404 Not Found p/ "nenhuma encontrada"
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro interno ao criar empresa");
        }
    }


    /**
     * CRIAR ADITIVO CONTRATUAL
     */
    @PostMapping("/aditivo/um-fiador")
    public ResponseEntity<Map<String, Object>> criarAditivoUmFiador(
            @RequestBody AditivoRequestDTO request) {

        log.info("📨 Recebida solicitação para aditivo com 1 fiador");

        AditivoResponseDTO response = correspondenciaService.solicitarCriacaoAditivo(
                request,
                false // 1 fiador
        );

        return construirRespostaSucesso(response, "Aditivo com 1 fiador criado com sucesso");
    }

    @PostMapping("/aditivo/dois-fiadores")
    public ResponseEntity<Map<String, Object>> criarAditivoDoisFiadores(
            @RequestBody AditivoRequestDTO request) {

        log.info("📨 Recebida solicitação para aditivo com 2 fiadores");

        AditivoResponseDTO response = correspondenciaService.solicitarCriacaoAditivo(
                request,
                true // 2 fiadores
        );

        return construirRespostaSucesso(response, "Aditivo com 2 fiadores criado com sucesso");
    }

    private ResponseEntity<Map<String, Object>> construirRespostaSucesso(AditivoResponseDTO response, String message) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("status", "success");
        resposta.put("message", message);
        resposta.put("aditivoId", response.getAditivoId());
        resposta.put("urlDownload", response.getUrlDownload());
        resposta.put("timestamp", Instant.now());

        return ResponseEntity.ok(resposta);
    }


}
