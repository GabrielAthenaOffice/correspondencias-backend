package com.recepcao.correspondencia.services.security;

import com.recepcao.correspondencia.dto.security.UserDTO;
import com.recepcao.correspondencia.dto.security.UserSimpleDTO;
import com.recepcao.correspondencia.entities.UserAthena;
import com.recepcao.correspondencia.mapper.UserMapper;
import com.recepcao.correspondencia.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserSimpleDTO> listarUsuarios() throws IllegalAccessException {
        List<UserAthena> usuarios = userRepository.findAll();

        if(usuarios.isEmpty()) {
            throw new IllegalAccessException("Nenhum usuario criado até o momento");
        }

        return usuarios.stream().map(usuario -> {
            UserSimpleDTO dto = new UserSimpleDTO();
            dto.setId(usuario.getId());
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());

            return dto;
        }).toList();
    }

    public Optional<UserDTO> buscarPorId(Long id) {
        UserAthena userEncontrado = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return Optional.of(UserMapper.toDTO(userEncontrado));
    }

}
