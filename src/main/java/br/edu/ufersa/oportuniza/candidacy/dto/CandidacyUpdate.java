package br.edu.ufersa.oportuniza.candidacy.dto;

import br.edu.ufersa.oportuniza.candidacy.CandidacyStatus;
import jakarta.validation.constraints.NotNull;

public record CandidacyUpdate(
        @NotNull(message = "O status da candidatura é obrigatório!")
        CandidacyStatus status
) {
}