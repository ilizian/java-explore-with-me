package ru.practicum.explore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NewUserRequest {
    @Email
    @NotNull
    @Size(max = 254, min = 6)
    private String email;
    @NotBlank
    @Size(max = 250, min = 2)
    private String name;
}