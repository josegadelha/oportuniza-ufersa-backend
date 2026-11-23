package br.edu.ufersa.oportuniza.projectproposal.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProjectProposalCreate(

        @NotBlank(message = "O título é obrigatório!")
        String title,

        @NotBlank(message = "A descrição é obrigatória!")
        String description,

        List<String> desiredSkills
) {
}
