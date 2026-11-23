package br.edu.ufersa.oportuniza.candidacy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class CandidacyInternalApiImpl implements CandidacyInternalApi {

    private final CandidacyRepository repository;

    CandidacyInternalApiImpl(CandidacyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findApprovedStudentIds(Long opportunityId) {
        return repository.findByOpportunityIdAndStatus(opportunityId, CandidacyStatus.APPROVED)
                .stream()
                .map(candidacy -> candidacy.getStudent().getId())
                .toList();
    }
}
