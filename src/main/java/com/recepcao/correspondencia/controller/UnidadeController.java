package com.recepcao.correspondencia.controller;

import com.recepcao.correspondencia.dto.UnidadeDTO;
import com.recepcao.correspondencia.services.arquivos.UnidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/unidades")
public class UnidadeController {

    @Autowired
    private UnidadeService unidadeService;

    @GetMapping
    public Set<String> listarUnidades() {
        return unidadeService.listarUnidades();
    }

    @GetMapping("/{nome}")
    public ResponseEntity<UnidadeDTO> getUnidade(@PathVariable String nome) {
        UnidadeService.UnidadeInfo info = unidadeService.getUnidadeInfo(nome);

        if (info == null) {
            return ResponseEntity.notFound().build();
        }

        UnidadeDTO dto = new UnidadeDTO(nome, info.cnpj(), info.endereco());

        return ResponseEntity.ok(dto);
    }
}
