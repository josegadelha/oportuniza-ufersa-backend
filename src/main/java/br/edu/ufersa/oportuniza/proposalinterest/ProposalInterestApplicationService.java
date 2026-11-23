package br.edu.ufersa.oportuniza.proposalinterest;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufersa.oportuniza.projectproposal.ProjectProposalInternalApi;
import br.edu.ufersa.oportuniza.projectproposal.ProjectProposalSummary;
import br.edu.ufersa.oportuniza.proposalinterest.dto.ProposalInterestPatch;
import br.edu.ufersa.oportuniza.proposalinterest.dto.ProposalInterestResponse;
import br.edu.ufersa.oportuniza.proposalinterest.dto.ProposalInterestUpdate;
import br.edu.ufersa.oportuniza.shared.exception.ResourceAccessDeniedException;
import br.edu.ufersa.oportuniza.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.oportuniza.user.User;

@Service
class ProposalInterestApplicationService {

    private final ProposalInterestRepository repository;
    private final ProjectProposalInternalApi projectProposalApi;
    private final ProposalInterestMapper mapper;
    private final ProposalInterestService domain;

    public ProposalInterestApplicationService(
            ProposalInterestRepository repository,
            ProjectProposalInternalApi projectProposalApi,
            ProposalInterestMapper mapper,
            ProposalInterestService domain
    ) {
        this.repository = repository;
        this.projectProposalApi = projectProposalApi;
        this.mapper = mapper;
        this.domain = domain;
    }

    @Transactional(readOnly = true)
    public List<ProposalInterestResponse> listMine(User user) {
        return mapper.toResponseList(repository.findByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public List<ProposalInterestResponse> listByProjectProposal(
            Long projectProposalId,
            User user
    ) {
        findProjectProposal(projectProposalId);
        domain.requireProposalOwner(projectProposalId, user);

        return mapper.toResponseList(
                repository.findByProjectProposalId(projectProposalId)
        );
    }

    @Transactional(readOnly = true)
    public ProposalInterestResponse findById(
            Long proposalInterestId,
            User user
    ) {
        ProposalInterest interest = findProposalInterest(proposalInterestId);
        domain.requireReader(interest, user);
        return mapper.toResponse(interest);
    }

    @Transactional
    public ProposalInterestResponse createForProjectProposal(
            Long projectProposalId,
            User user
    ) {
        ProjectProposalSummary projectProposal =
                findProjectProposal(projectProposalId);
        if (projectProposal.studentId().equals(user.getId())) {
            throw new ResourceAccessDeniedException(
                    "O autor da proposta não pode demonstrar interesse na própria proposta."
            );
        }
        ProposalInterest saved = repository.save(mapper.toEntity(projectProposal.id(), user));
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProposalInterestResponse update(
            Long proposalInterestId,
            ProposalInterestUpdate dto,
            User user
    ) {
        ProposalInterest interest = findProposalInterest(proposalInterestId);
        domain.requireProposalOwner(interest.getProjectProposalId(), user);
        interest.updateStatus(dto.status());
        return mapper.toResponse(repository.save(interest));
    }

    @Transactional
    public ProposalInterestResponse partialUpdate(
            Long proposalInterestId,
            ProposalInterestPatch dto,
            User user
    ) {
        ProposalInterest interest = findProposalInterest(proposalInterestId);
        domain.requireProposalOwner(interest.getProjectProposalId(), user);
        if (dto.status() != null) {
            interest.updateStatus(dto.status());
        }
        return mapper.toResponse(repository.save(interest));
    }

    @Transactional
    public void remove(
            Long proposalInterestId,
            User user
    ) {
        ProposalInterest proposalInterest =
                findProposalInterest(proposalInterestId);
        domain.requireDeleteAccess(proposalInterest, user);
        repository.delete(proposalInterest);
    }

    private ProposalInterest findProposalInterest(
            Long proposalInterestId
    ) {
        return repository
                .findById(proposalInterestId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Interesse em proposta não encontrado."
                        )
                );
    }

    private ProjectProposalSummary findProjectProposal(
            Long projectProposalId
    ) {
        return projectProposalApi.findById(projectProposalId);
    }
}