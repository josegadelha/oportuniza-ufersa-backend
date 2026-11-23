package br.edu.ufersa.oportuniza.proposalinterest.dto;

import br.edu.ufersa.oportuniza.proposalinterest.InterestStatus;

public record ProposalInterestPatch(
        InterestStatus status
) {
}
