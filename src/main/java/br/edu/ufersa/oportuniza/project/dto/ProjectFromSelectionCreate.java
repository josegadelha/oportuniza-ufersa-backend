package br.edu.ufersa.oportuniza.project.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProjectFromSelectionCreate(

        @NotNull(message = "A data de início é obrigatória.")
        LocalDate startDate
) {
}
