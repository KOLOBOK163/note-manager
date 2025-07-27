package com.ksbk.auth.mapper;


import com.ksbk.auth.DTO.UserDTO;
import com.ksbk.auth.entity.User;
import com.ksbk.auth.DTO.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    UserDTO UserEntityToUserDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User UserDTOToEntity(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    List<UserResponse> UsersEntityToUsersResponse(List<User> user);
}
