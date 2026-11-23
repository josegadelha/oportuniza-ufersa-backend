package br.edu.ufersa.oportuniza.opportunity;

public interface OpportunityInternalApi {

    OpportunitySummary findById(Long opportunityId);

    OpportunitySummary lockForSelectionFinalization(Long opportunityId);

    OpportunitySummary lockForCandidacyUpdate(Long opportunityId);

    void finishSelection(Long opportunityId);

    boolean existsById(Long opportunityId);
}
