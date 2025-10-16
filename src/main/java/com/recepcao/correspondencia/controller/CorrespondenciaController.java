package com.recepcao.correspondencia.controller;

import com.recepcao.correspondencia.config.APIExceptions;
import com.recepcao.correspondencia.dto.CorrespondenciaComEmpresaDTO;
import com.recepcao.correspondencia.dto.CorrespondenciaResponse;
import com.recepcao.correspondencia.dto.contracts.EmailServiceDTO;
import com.recepcao.correspondencia.dto.record.EmailResponseRecord;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.Correspondencia;
import com.recepcao.correspondencia.entities.Empresa;
import com.recepcao.correspondencia.feign.AditivoRequestDTO;
import com.recepcao.correspondencia.feign.AditivoResponseDTO;
import com.recepcao.correspondencia.mapper.enums.StatusCorresp;
import com.recepcao.correspondencia.repositories.CorrespondenciaRepository;
import com.recepcao.correspondencia.repositories.EmpresaRepository;
import com.recepcao.correspondencia.services.CorrespondenciaService;
import com.recepcao.correspondencia.services.arquivos.StorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/correspondencias")
@RequiredArgsConstructor
public class CorrespondenciaController {

    private final CorrespondenciaService correspondenciaService;
    private final StorageService storageService;
    private final EmpresaRepository empresaRepository;
    private final CorrespondenciaRepository correspondenciaRepository;

    /**
     * // === CRIAÇÃO DE CORRESPONDÊNCIA ===
     */
    @PostMapping("/processar-correspondencia")
    public ResponseEntity<Correspondencia> receberCorrespondencia(@Valid @RequestBody Correspondencia correspondencia) {
        Correspondencia salva = correspondenciaService.processarCorrespondencia(correspondencia);
        return ResponseEntity.ok(salva);
    }

    @PostMapping(value = "/receber", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Correspondencia> receberCorrespondenciaComArquivos(
            @RequestPart("nomeEmpresa") String nomeEmpresa,
            @RequestPart("remetente") String remetente,
            @RequestPart(value = "arquivos", required = false) List<MultipartFile> arquivos
    ) {
        List<String> keys = (arquivos == null || arquivos.isEmpty())
                ? List.of()
                : storageService.salvarMuitos(arquivos);

        Correspondencia c = new Correspondencia();
        c.setNomeEmpresaConexa(nomeEmpresa == null ? null : nomeEmpresa.trim());
        c.setRemetente(remetente == null ? null : remetente.trim());
        c.setStatusCorresp(StatusCorresp.ANALISE);
        c.setDataRecebimento(LocalDateTime.now(ZoneId.of("America/Recife")));
        c.setAnexos(keys);

        Correspondencia salvo = correspondenciaService.processarCorrespondencia(c);
        return new ResponseEntity<>(salvo, HttpStatus.OK);
    }

    /**
     * // === LISTAGENS ===
     */
    @GetMapping("/listar-correspondencia")
    public ResponseEntity<CorrespondenciaResponse> listarCorrespondencias(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        CorrespondenciaResponse correspondenciaResponse = correspondenciaService.listarTodas(pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(correspondenciaResponse);
    }

    @GetMapping("/listar-com-empresa")
    public ResponseEntity<List<CorrespondenciaComEmpresaDTO>> listarCorrespondenciasComEmpresa(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        List<CorrespondenciaComEmpresaDTO> correspondenciasComEmpresa =
                correspondenciaService.listarComEmpresa(pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(correspondenciasComEmpresa);
    }

    /**
     * // === EMPRESAS (Conexa) ===
     */
    @GetMapping("/buscar-empresa")
    public ResponseEntity<List<CustomerResponse>> buscarEmpresasNoConexa(@NotNull @RequestParam String nomeEmpresa) {
        List<CustomerResponse> result = correspondenciaService.verificarEmpresaConexa(nomeEmpresa);
        return ResponseEntity.ok(result);
    }

    /**
     * // === ATUALIZAÇÕES ===
     */
    @PutMapping("/{id}/data-aviso")
    public ResponseEntity<Correspondencia> atualizarDataAviso(
            @PathVariable Long id,
            @RequestParam(required = false) String dataAviso
    ) {
        try {
            LocalDate data = (dataAviso != null && !dataAviso.trim().isEmpty())
                    ? LocalDate.parse(dataAviso)
                    : null;
            Correspondencia correspondencia = correspondenciaService.atualizarDataAviso(id, data);
            return ResponseEntity.ok(correspondencia);
        } catch (APIExceptions e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Correspondencia> atualizarCorrespondencia(
            @PathVariable Long id,
            @RequestBody Correspondencia updates
    ) {
        try {
            Correspondencia atualizada = correspondenciaService.atualizarCorrespondencia(id, updates);
            return ResponseEntity.ok(atualizada);
        } catch (APIExceptions e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> apagarCorrespondencia(@PathVariable Long id) {
        try {
            correspondenciaService.apagarCorrespondencia(id);
            return ResponseEntity.ok().build();
        } catch (APIExceptions e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Correspondência não encontrada");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao apagar correspondência");
        }
    }

    /**
     *  === ENVIO DE AVISO ===
     */
    @PostMapping("/enviar-aviso-resend")
    public ResponseEntity<EmailResponseRecord> enviarAvisoResend(@RequestBody EmailServiceDTO dto) {
        return ResponseEntity.ok(correspondenciaService.envioEmailCorrespondenciaResend(dto));
    }


    @PostMapping(
            value = "/{id}/enviar-aviso-resend-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EmailResponseRecord> enviarAvisoResendUpload(
            @PathVariable Long id,
            @RequestPart(value = "arquivos", required = false) List<MultipartFile> arquivos
    ) {
        var corr = correspondenciaRepository.findById(id)
                .orElseThrow(() -> new APIExceptions("Correspondência não encontrada"));

        var resp = correspondenciaService.envioEmailCorrespondenciaResendUpload(
                corr.getNomeEmpresaConexa(),
                arquivos
        );

        return ResponseEntity.ok(resp);
    }
}
