package br.edu.ufersa.oportuniza.projectproposal;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufersa.oportuniza.projectproposal.dto.ProjectProposalCreate;
import br.edu.ufersa.oportuniza.projectproposal.dto.ProjectProposalPatch;
import br.edu.ufersa.oportuniza.projectproposal.dto.ProjectProposalResponse;
import br.edu.ufersa.oportuniza.projectproposal.dto.ProjectProposalUpdate;
import br.edu.ufersa.oportuniza.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.oportuniza.student.Student;
import br.edu.ufersa.oportuniza.user.User;

@Service
class ProjectProposalApplicationService {

    private final ProjectProposalRepository repository;
    private final ProjectProposalMapper mapper;
    private final ProjectProposalService domain;

    public ProjectProposalApplicationService(
            ProjectProposalRepository repository,
            ProjectProposalMapper mapper,
            ProjectProposalService domain
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.domain = domain;
    }

    @Transactional(readOnly = true)
    public List<ProjectProposalResponse> list() {
        return mapper.toResponseList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public List<ProjectProposalResponse> listByStudent(User user) {
        Student student = domain.requireStudent(user);
        return mapper.toResponseList(repository.findByStudentId(student.getId()));
    }

    @Transactional(readOnly = true)
    public ProjectProposalResponse findById(Long projectProposalId) {
        return mapper.toResponse(findProjectProposal(projectProposalId));
    }

    @Transactional
    public ProjectProposalResponse createForStudent(User user, ProjectProposalCreate dto) {
        Student student = domain.requireStudent(user);
        ProjectProposal saved = repository.save(mapper.toEntity(student, dto));
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProjectProposalResponse update(Long projectProposalId, ProjectProposalUpdate dto, User user) {
        ProjectProposal current = findProjectProposal(projectProposalId);
        domain.requireOwner(current, user);
        current.updateDetails(dto.title(), dto.description(), dto.desiredSkills());
        current.updateStatus(dto.status());
        return mapper.toResponse(repository.save(current));
    }

    @Transactional
    public ProjectProposalResponse partialUpdate(Long projectProposalId, ProjectProposalPatch dto, User user) {
        ProjectProposal current = findProjectProposal(projectProposalId);
        domain.requireOwner(current, user);

        String title = current.getTitle();
        String description = current.getDescription();
        List<String> desiredSkills = current.getDesiredSkills();

        if (dto.title() != null) {
            title = dto.title();
        }
        if (dto.description() != null) {
            description = dto.description();
        }
        if (dto.desiredSkills() != null) {
            desiredSkills = dto.desiredSkills();
        }

        if (dto.title() != null || dto.description() != null || dto.desiredSkills() != null) {
            current.updateDetails(title, description, desiredSkills);
        }
        if (dto.status() != null) {
            current.updateStatus(dto.status());
        }
        return mapper.toResponse(repository.save(current));
    }

    @Transactional
    public void remove(Long projectProposalId, User user) {
        ProjectProposal proposal = findProjectProposal(projectProposalId);
        domain.requireOwner(proposal, user);
        repository.delete(proposal);
    }

    private ProjectProposal findProjectProposal(Long projectProposalId) {
        return repository.findById(projectProposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta de projeto não encontrada."));
    }

}
