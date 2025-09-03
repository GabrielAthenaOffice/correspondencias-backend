package com.recepcao.correspondencia.controller;

import com.recepcao.correspondencia.clients.AppConstants;
import com.recepcao.correspondencia.clients.ConexaClients;
import com.recepcao.correspondencia.dto.EmpresaResponse;
import com.recepcao.correspondencia.dto.contracts.CustomerWithContractResponse;
import com.recepcao.correspondencia.dto.responses.ConexaCustomerListResponse;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.Empresa;
import com.recepcao.correspondencia.mapper.enums.Situacao;
import com.recepcao.correspondencia.mapper.enums.StatusEmpresa;
import com.recepcao.correspondencia.services.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final ConexaClients conexaClients;

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
}
