package br.edu.ufersa.oportuniza.proposalinterest.dto;

import br.edu.ufersa.oportuniza.proposalinterest.InterestStatus;
import jakarta.validation.constraints.NotNull;

public record ProposalInterestUpdate(
        @NotNull(message = "O status do interesse é obrigatório!")
        InterestStatus status
) {
}
