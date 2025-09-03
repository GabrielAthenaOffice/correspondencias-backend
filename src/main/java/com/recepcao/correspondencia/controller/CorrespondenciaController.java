package com.recepcao.correspondencia.controller;

import com.recepcao.correspondencia.config.APIExceptions;
import com.recepcao.correspondencia.dto.CorrespondenciaComEmpresaDTO;
import com.recepcao.correspondencia.dto.CorrespondenciaResponse;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.Correspondencia;
import com.recepcao.correspondencia.mapper.enums.StatusCorresp;
import com.recepcao.correspondencia.services.CorrespondenciaService;
import com.recepcao.correspondencia.services.arquivos.StorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
// import removido: ResponseStatusException não é utilizado neste controller

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/correspondencias")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
public class CorrespondenciaController {

    private final CorrespondenciaService correspondenciaService;
    private final StorageService storageService;

    /**
     * ✅ Recebe uma nova correspondência e processa (regra de negócio completa).
     */
    @PostMapping("/processar-correspondencia")
    public ResponseEntity<Correspondencia> receberCorrespondencia(@Valid @RequestBody Correspondencia correspondencia) {
        Correspondencia salva = correspondenciaService.processarCorrespondencia(correspondencia);
        return ResponseEntity.ok(salva);
    }

    @PostMapping(value = "/processar-correspondencia/receber-com-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Correspondencia> receberCorrespondenciaComFoto(
            @NotNull @RequestParam String nomeEmpresa,
            @NotNull @RequestParam String remetente,
            @RequestParam(required = false) MultipartFile foto
    ) {
        // Salva a foto no servidor/local (no futuro, no S3)
        String caminhoFoto = null;
        if (foto != null && !foto.isEmpty()) {
            caminhoFoto = storageService.salvarFoto(foto);
        }

        Correspondencia correspondencia = new Correspondencia();
        correspondencia.setNomeEmpresaConexa(nomeEmpresa);
        correspondencia.setRemetente(remetente);
        correspondencia.setFotoCorrespondencia(caminhoFoto);
        correspondencia.setStatusCorresp(StatusCorresp.ANALISE);
        correspondencia.setDataRecebimento(LocalDate.now());

        Correspondencia processada = correspondenciaService.processarCorrespondencia(correspondencia);

        return ResponseEntity.ok(processada);
    }


    /**
     * Lista todas as correspondências salvas no banco.
     */
    @GetMapping("/listar-correspondencia")
    public ResponseEntity<CorrespondenciaResponse> listarCorrespondencias(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                                          @RequestParam(name = "pageSize", defaultValue = "50", required = false) Integer pageSize,
                                                                          @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
                                                                          @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {
        CorrespondenciaResponse correspondenciaResponse = correspondenciaService.listarTodas(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(correspondenciaResponse, HttpStatus.OK);
    }

    /**
     * Lista correspondências com dados da empresa associada.
     */
    @GetMapping("/listar-com-empresa")
    public ResponseEntity<List<CorrespondenciaComEmpresaDTO>> listarCorrespondenciasComEmpresa(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                                                               @RequestParam(name = "pageSize", defaultValue = "50", required = false) Integer pageSize,
                                                                                               @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
                                                                                               @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {
        List<CorrespondenciaComEmpresaDTO> correspondenciasComEmpresa = correspondenciaService.listarComEmpresa(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(correspondenciasComEmpresa, HttpStatus.OK);
    }

    /**
     * Busca empresas no Conexa pelo nome. Útil para testes rápidos da API Conexa.
     */
    @GetMapping("/buscar-empresa")
    public ResponseEntity<List<CustomerResponse>> buscarEmpresasNoConexa(@NotNull @RequestParam String nomeEmpresa) {
        List<CustomerResponse> result = correspondenciaService.verificarEmpresaConexa(nomeEmpresa);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint de teste simples para verificar se o controller está funcionando
     */
    @GetMapping("/teste")
    public ResponseEntity<String> teste() {
        System.out.println("=== ENDPOINT DE TESTE CHAMADO ===");
        return ResponseEntity.ok("Controller funcionando!");
    }

    /**
     * Endpoint de teste para verificar se o arquivo existe
     * Exemplo: GET /api/correspondencias/teste-arquivo/{nomeArquivo}
     */
    @GetMapping("/teste-arquivo/{nomeArquivo}")
    public ResponseEntity<String> testeArquivo(@PathVariable String nomeArquivo) {
        try {
            String currentDir = System.getProperty("user.dir");
            Path filePath = Paths.get(currentDir, "uploads", "correspondencias", nomeArquivo).normalize();
            
            System.out.println("=== TESTE ARQUIVO ===");
            System.out.println("Nome do arquivo: " + nomeArquivo);
            System.out.println("Diretório atual: " + currentDir);
            System.out.println("Caminho completo: " + filePath.toString());
            System.out.println("Arquivo existe: " + Files.exists(filePath));
            System.out.println("===================");
            
            if (Files.exists(filePath)) {
                return ResponseEntity.ok("Arquivo encontrado: " + filePath.toString());
            } else {
                return ResponseEntity.ok("Arquivo NÃO encontrado: " + filePath.toString());
            }
        } catch (Exception e) {
            System.out.println("Erro no teste: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("Erro: " + e.getMessage());
        }
    }

    /**
     * Endpoint para servir arquivos de correspondência (imagens, PDFs, etc)
     * Exemplo: GET /api/correspondencias/arquivo/{nomeArquivo}
     */
    @GetMapping("/arquivo/{nomeArquivo}")
    public ResponseEntity<Resource> servirArquivo(@PathVariable String nomeArquivo) {
        try {
            // Validar o nome do arquivo para evitar path traversal
            if (nomeArquivo == null || nomeArquivo.trim().isEmpty() || nomeArquivo.contains("..")) {
                System.out.println("Nome do arquivo inválido: " + nomeArquivo);
                return ResponseEntity.badRequest().build();
            }
            
            // Usar caminho absoluto baseado no diretório do projeto
            String currentDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(currentDir, "uploads", "correspondencias").normalize();
            Path filePath = uploadDir.resolve(nomeArquivo).normalize();
            
            // Verificar se o arquivo está dentro do diretório de uploads
            if (!filePath.startsWith(uploadDir)) {
                System.out.println("Tentativa de acesso fora do diretório permitido: " + filePath);
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Tentando acessar arquivo: " + filePath.toString());
            
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                System.out.println("Arquivo não encontrado: " + filePath.toString());
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Arquivo encontrado: " + filePath.toString());
            
            // Detecta o tipo do arquivo
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            System.out.println("Erro ao servir arquivo: " + nomeArquivo);
            e.printStackTrace(); // Adicionado para logar o erro
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint alternativo para servir arquivos diretamente
     * Exemplo: GET /api/correspondencias/uploads/{nomeArquivo}
     */
    @GetMapping("/uploads/{nomeArquivo}")
    public ResponseEntity<Resource> servirArquivoDireto(@PathVariable String nomeArquivo) {
        try {
            // Usar caminho absoluto
            String currentDir = System.getProperty("user.dir");
            Path filePath = Paths.get(currentDir, "uploads", "correspondencias", nomeArquivo).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            // Detecta o tipo do arquivo
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Atualiza a data de aviso de uma correspondência
     */
    @PutMapping("/{id}/data-aviso")
    public ResponseEntity<Correspondencia> atualizarDataAviso(
            @PathVariable Long id,
            @RequestParam(required = false) String dataAviso
    ) {
        try {
            LocalDate data = null;
            if (dataAviso != null && !dataAviso.trim().isEmpty()) {
                try {
                    data = LocalDate.parse(dataAviso);
                } catch (Exception e) {
                    System.err.println("Erro ao fazer parse da data: " + dataAviso + " - " + e.getMessage());
                    return ResponseEntity.badRequest().build();
                }
            }
            
            Correspondencia correspondencia = correspondenciaService.atualizarDataAviso(id, data);
            return ResponseEntity.ok(correspondencia);
        } catch (APIExceptions e) {
            System.err.println("Erro ao atualizar data de aviso para ID " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Erro interno ao atualizar data de aviso para ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Atualiza campos de uma correspondência (parcial).
     * Ex: PUT /api/correspondencias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Correspondencia> atualizarCorrespondencia(@PathVariable Long id, @RequestBody Correspondencia updates) {
        try {
            Correspondencia atualizada = correspondenciaService.atualizarCorrespondencia(id, updates);
            return ResponseEntity.ok(atualizada);
        } catch (APIExceptions e) {
            System.err.println("Erro ao atualizar correspondência ID " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Erro interno ao atualizar correspondência ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> apagarCorrespondencia(@PathVariable Long id) {
        System.out.println("[CorrespondenciaController] Recebida requisição DELETE /api/correspondencias/" + id);
        try {
            correspondenciaService.apagarCorrespondencia(id);
            System.out.println("[CorrespondenciaController] Correspondência " + id + " apagada com sucesso");
            return ResponseEntity.ok().build();
        } catch (APIExceptions e) {
            System.err.println("[CorrespondenciaController] Não encontrada correspondência " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Correspondência não encontrada");
        } catch (Exception e) {
            System.err.println("[CorrespondenciaController] Erro ao apagar correspondência " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao apagar correspondência");
        }
    }

}
