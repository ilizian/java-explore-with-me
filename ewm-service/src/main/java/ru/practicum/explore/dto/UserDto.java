package ru.practicum.explore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank
    @Size(min = 1, max = 512)
    private String email;
    @NotBlank
    @Size(min = 1, max = 255)
    private String name;
}
