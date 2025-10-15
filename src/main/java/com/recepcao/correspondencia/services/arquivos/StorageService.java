package com.recepcao.correspondencia.services.arquivos;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/correspondencias/";

    public String salvarFoto(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String fileName = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName; // <- guarde ISSO no banco (chave)
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar a foto da correspondência", e);
        }
    }

    public List<String> salvarMuitos(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        List<String> out = new ArrayList<>();
        for (MultipartFile f : files) {
            String key = salvarFoto(f);
            if (key != null) out.add(key);
        }
        return out;
    }

    public byte[] read(String key) {
        try {
            return Files.readAllBytes(resolvePath(key));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler arquivo: " + key, e);
        }
    }

    public String contentType(String key) {
        try {
            String ct = Files.probeContentType(resolvePath(key));
            return ct != null ? ct : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    public String originalFilename(String key) {
        // key salvo é "UUID_original.ext" -> devolve "original.ext"
        int idx = key.indexOf('_');
        return (idx > -1 && idx < key.length() - 1) ? key.substring(idx + 1) : key;
    }

    public boolean exists(String key) {
        return Files.exists(resolvePath(key));
    }

    public Path resolvePath(String key) {
        return Paths.get(UPLOAD_DIR).resolve(key);
    }

    private String sanitize(String name) {
        if (name == null) return "arquivo.bin";
        // remove path traversal e normaliza
        String n = name.replace("\\", "/");
        n = n.substring(n.lastIndexOf('/') + 1);
        return n.isBlank() ? "arquivo.bin" : n;
    }
}

