package com.mitocode.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PatientDTO {

    @EqualsAndHashCode.Include
    private Integer idPatient;

    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 3, max = 70, message = "{firstname.size}")
    private String firstName;

    @NotNull
    @NotEmpty
    @Size(min = 3, max = 70, message = "{lastname.size}")
    private String lastName;

    @NotNull
    @NotEmpty
    private String dni;

    @NotNull
    @NotEmpty
    private String address;

    @NotNull
    @NotEmpty
    @Pattern(regexp = "[0-9]+")
    private String phone;

    @Email
    private String email;
}
