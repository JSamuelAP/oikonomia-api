package dev.jsamuelap.oikonomiaapi.user.infrastructure.in.rest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import dev.jsamuelap.oikonomiaapi.user.domain.port.in.AuthenticateUserCommand;
import dev.jsamuelap.oikonomiaapi.user.domain.port.in.RegisterUserCommand;

@Mapper(componentModel = "spring")
public interface UserRestMapper {
  @Mapping(target = "rawPassword", source = "password")
  RegisterUserCommand toCommand(RegisterUserRequest request);

  @Mapping(target = "rawPassword", source = "password")
  AuthenticateUserCommand toCommand(AuthenticateUserRequest request);
}
