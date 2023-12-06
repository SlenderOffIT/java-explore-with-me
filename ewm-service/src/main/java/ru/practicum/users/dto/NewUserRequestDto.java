package ru.practicum.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.*;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PACKAGE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewUserRequestDto {
    @NotBlank
    @Size(min = 2, max = 250)
    String name;
    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    String email;
}
