package br.edu.ufersa.oportuniza.projectproposal.dto;

import br.edu.ufersa.oportuniza.proposal.ProposalStatus;

import jakarta.validation.constraints.Size;

import java.util.List;

public record ProjectProposalPatch(

        @Size(min = 1, message = "O título não pode ser vazio!")
        String title,

        @Size(min = 1, message = "A descrição não pode ser vazia!")
        String description,

        ProposalStatus status,

        List<String> desiredSkills
) {
}
