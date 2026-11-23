package br.edu.ufersa.oportuniza.candidacy;

import java.util.List;

public interface CandidacyInternalApi {

    List<Long> findApprovedStudentIds(Long opportunityId);
}
