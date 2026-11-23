package br.edu.ufersa.oportuniza.candidacy;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufersa.oportuniza.candidacy.dto.CandidacyPatch;
import br.edu.ufersa.oportuniza.candidacy.dto.CandidacyResponse;
import br.edu.ufersa.oportuniza.candidacy.dto.CandidacyUpdate;
import br.edu.ufersa.oportuniza.opportunity.OpportunityInternalApi;
import br.edu.ufersa.oportuniza.opportunity.OpportunityStatus;
import br.edu.ufersa.oportuniza.opportunity.OpportunitySummary;
import br.edu.ufersa.oportuniza.notification.NotificationApplicationService;
import br.edu.ufersa.oportuniza.notification.NotificationType;
import br.edu.ufersa.oportuniza.shared.exception.BusinessRuleViolationException;
import br.edu.ufersa.oportuniza.shared.exception.DuplicateResourceException;
import br.edu.ufersa.oportuniza.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.oportuniza.student.Student;
import br.edu.ufersa.oportuniza.user.User;

@Service
class CandidacyApplicationService {

    private final CandidacyRepository repository;
    private final OpportunityInternalApi opportunityApi;
    private final CandidacyMapper mapper;
    private final CandidacyService domain;
    private final NotificationApplicationService notifications;

    public CandidacyApplicationService(
            CandidacyRepository repository,
            OpportunityInternalApi opportunityApi,
            CandidacyMapper mapper,
            CandidacyService domain,
            NotificationApplicationService notifications
    ) {
        this.repository = repository;
        this.opportunityApi = opportunityApi;
        this.mapper = mapper;
        this.domain = domain;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<CandidacyResponse> listByOpportunity(Long opportunityId, User user) {
        domain.requireOpportunityOwner(findOpportunity(opportunityId), user);
        return mapper.toResponseList(repository.findByOpportunityId(opportunityId));
    }

    @Transactional(readOnly = true)
    public List<CandidacyResponse> listByStudent(User user) {
        Student student = domain.requireStudent(user);
        return mapper.toResponseList(repository.findByStudentId(student.getId()));
    }

    @Transactional(readOnly = true)
    public CandidacyResponse findById(Long candidacyId, User user) {
        Candidacy candidacy = findCandidacy(candidacyId);
        domain.requireReader(candidacy, user);
        return mapper.toResponse(candidacy);
    }

    @Transactional
    public CandidacyResponse createForOpportunity(Long opportunityId, User user) {
        OpportunitySummary opportunity = opportunityApi.lockForCandidacyUpdate(opportunityId);
        Student student = domain.requireStudent(user);
        if (opportunity.status() != OpportunityStatus.OPEN) {
            throw new BusinessRuleViolationException("Somente oportunidades abertas aceitam candidaturas.");
        }

        repository.findByStudentIdAndOpportunityId(student.getId(), opportunity.id())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Este estudante já se candidatou a esta oportunidade.");
                });

        Candidacy saved = repository.save(mapper.toEntity(student, opportunity.id()));
        return mapper.toResponse(saved);
    }

    @Transactional
    public CandidacyResponse update(Long candidacyId, CandidacyUpdate dto, User user) {
        Candidacy current = findCandidacy(candidacyId);
        OpportunitySummary opportunity = opportunityApi.lockForCandidacyUpdate(current.getOpportunityId());
        domain.requireOpportunityOwner(opportunity, user);
        requireSelectionInProgress(opportunity);
        current.updateStatus(dto.status());
        Candidacy saved = repository.save(current);
        notifyStatusChange(saved, opportunity.title());
        return mapper.toResponse(saved);
    }

    @Transactional
    public CandidacyResponse partialUpdate(Long candidacyId, CandidacyPatch dto, User user) {
        Candidacy current = findCandidacy(candidacyId);
        OpportunitySummary opportunity = opportunityApi.lockForCandidacyUpdate(current.getOpportunityId());
        domain.requireOpportunityOwner(opportunity, user);
        requireSelectionInProgress(opportunity);
        if (dto.status() != null) {
            current.updateStatus(dto.status());
        }

        Candidacy saved = repository.save(current);
        if (dto.status() != null) {
            notifyStatusChange(saved, opportunity.title());
        }
        return mapper.toResponse(saved);
    }

    @Transactional
    public void remove(Long candidacyId, User user) {
        Candidacy candidacy = findCandidacy(candidacyId);
        OpportunitySummary opportunity = opportunityApi.lockForCandidacyUpdate(candidacy.getOpportunityId());
        domain.requireReader(candidacy, user);
        requireSelectionInProgress(opportunity);
        repository.delete(candidacy);
    }

    private Candidacy findCandidacy(Long candidacyId) {
        return repository.findById(candidacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidatura não encontrada."));
    }

    private OpportunitySummary findOpportunity(Long opportunityId) {
        return opportunityApi.findById(opportunityId);
    }

    private void requireSelectionInProgress(OpportunitySummary opportunity) {
        if (opportunity.status() == OpportunityStatus.FINISHED
                || opportunity.status() == OpportunityStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Não é permitido alterar candidaturas após a finalização da seleção."
            );
        }
    }

    private void notifyStatusChange(Candidacy candidacy, String opportunityTitle) {
        notifications.notifyUser(
                candidacy.getStudent(),
                "Atualização da candidatura",
                "Sua candidatura à oportunidade \"" + opportunityTitle + "\""
                        + " foi atualizada para " + candidacy.getStatus() + ".",
                NotificationType.SELECTION_PROCESS_UPDATE
        );
    }
}
