package com.picpay.challenge.app.dtos;

import com.picpay.challenge.app.domain.user.UserType;

import java.math.BigDecimal;

public record UserDTO(String firstName,
                      String lastName,
                      String document,
                      BigDecimal balance,
                      String email,
                      UserType userType,
                      String password) {
}
