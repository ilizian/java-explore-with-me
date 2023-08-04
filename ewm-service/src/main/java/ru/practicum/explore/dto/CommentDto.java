package ru.practicum.explore.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private UserShortDto author;
    private EventShortDto event;
    @NotBlank
    private String message;
    private String created;
    private String updated;
}