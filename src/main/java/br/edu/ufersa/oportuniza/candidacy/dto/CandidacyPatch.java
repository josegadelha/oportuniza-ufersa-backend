package br.edu.ufersa.oportuniza.candidacy.dto;

import br.edu.ufersa.oportuniza.candidacy.CandidacyStatus;

public record CandidacyPatch(
        CandidacyStatus status
) {
}