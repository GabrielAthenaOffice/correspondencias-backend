package com.recepcao.correspondencia.mapper;

import com.recepcao.correspondencia.dto.security.UserCreateDTO;
import com.recepcao.correspondencia.dto.security.UserDTO;
import com.recepcao.correspondencia.dto.security.UserSimpleDTO;
import com.recepcao.correspondencia.entities.UserAthena;

public class UserMapper {

    public static UserDTO toDTO(UserAthena user) {
        return new UserDTO(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getSenha(),
                user.getCargo(),
                user.getRole()
        );
    }

    public static UserAthena toEntity(UserCreateDTO dto) {
        UserAthena user = new UserAthena();
        user.setNome(dto.getNome());
        user.setEmail(dto.getEmail());
        user.setSenha(dto.getSenha());
        user.setCargo(dto.getCargo());
        user.setRole(dto.getRole());
        return user;
    }

    public static UserSimpleDTO toSimpleDTO(UserAthena user) {
        return new UserSimpleDTO(user.getId(), user.getNome(), user.getEmail());
    }

    public static UserAthena toUser(UserDTO dto) {
        UserAthena user = new UserAthena();
        user.setNome(dto.getNome());
        user.setEmail(dto.getEmail());
        user.setSenha(dto.getSenha());
        user.setCargo(dto.getCargo());
        user.setRole(dto.getRole());
        return user;
    }

}
