package com.recepcao.correspondencia.services.arquivos;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/correspondencias/";

    public String salvarFoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            
            // Criar diretório se não existir
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;  // Retorna apenas o nome do arquivo
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar a foto da correspondência", e);
        }
    }
}

