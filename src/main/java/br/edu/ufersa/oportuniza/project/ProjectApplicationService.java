package br.edu.ufersa.oportuniza.project;

import br.edu.ufersa.oportuniza.candidacy.CandidacyInternalApi;
import br.edu.ufersa.oportuniza.opportunity.OpportunityInternalApi;
import br.edu.ufersa.oportuniza.opportunity.OpportunityStatus;
import br.edu.ufersa.oportuniza.opportunity.OpportunitySummary;
import br.edu.ufersa.oportuniza.professor.Professor;
import br.edu.ufersa.oportuniza.professor.ProfessorRepository;
import br.edu.ufersa.oportuniza.project.dto.ProjectFromSelectionCreate;
import br.edu.ufersa.oportuniza.project.dto.ProjectPatch;
import br.edu.ufersa.oportuniza.project.dto.ProjectResponse;
import br.edu.ufersa.oportuniza.project.dto.ProjectUpdate;
import br.edu.ufersa.oportuniza.shared.exception.ProjectRuleViolationException;
import br.edu.ufersa.oportuniza.shared.exception.ResourceAccessDeniedException;
import br.edu.ufersa.oportuniza.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.oportuniza.student.Student;
import br.edu.ufersa.oportuniza.student.StudentRepository;
import br.edu.ufersa.oportuniza.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectApplicationService {

    private final ProjectRepository repository;
    private final ProjectService domain;
    private final ProjectMapper mapper;
    private final OpportunityInternalApi opportunities;
    private final CandidacyInternalApi candidacies;
    private final ProfessorRepository professors;
    private final StudentRepository students;

    public ProjectApplicationService(ProjectRepository repository, ProjectService domain,
                                     ProjectMapper mapper, OpportunityInternalApi opportunities,
                                     CandidacyInternalApi candidacies, ProfessorRepository professors,
                                     StudentRepository students) {
        this.repository = repository;
        this.domain = domain;
        this.mapper = mapper;
        this.opportunities = opportunities;
        this.candidacies = candidacies;
        this.professors = professors;
        this.students = students;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listMine(User user) {
        List<Project> projects = user instanceof Professor
                ? repository.findByAdvisorsId(user.getId())
                : repository.findByMembersId(user.getId());
        return mapper.toResponseList(projects);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long projectId, User user) {
        Project project = findProject(projectId);
        domain.requireParticipant(project, user);
        return mapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse update(Long projectId, ProjectUpdate dto, User user) {
        Project project = findProject(projectId);
        domain.requireAdvisor(project, user);
        mapper.updateEntityFromDto(dto, project);
        return mapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse patch(Long projectId, ProjectPatch dto, User user) {
        Project project = findProject(projectId);
        domain.requireAdvisor(project, user);
        mapper.updateEntityFromDto(dto, project);
        return mapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse complete(Long projectId, User user) {
        Project project = findProject(projectId);
        domain.requireAdvisor(project, user);
        project.complete();
        return mapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse cancel(Long projectId, User user) {
        Project project = findProject(projectId);
        domain.requireAdvisor(project, user);
        project.cancel();
        return mapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse createFromSelection(Long opportunityId, ProjectFromSelectionCreate dto, User user) {
        OpportunitySummary opportunity = opportunities.lockForSelectionFinalization(opportunityId);
        if (!(user instanceof Professor) || !opportunity.professorId().equals(user.getId())) {
            throw new ResourceAccessDeniedException("Somente o professor responsável pode finalizar esta seleção.");
        }
        if (opportunity.status() != OpportunityStatus.OPEN && opportunity.status() != OpportunityStatus.CLOSED) {
            throw new ProjectRuleViolationException(
                    "A seleção desta oportunidade já foi finalizada ou ainda não está disponível."
            );
        }

        List<Long> approvedStudentIds = candidacies.findApprovedStudentIds(opportunityId);
        if (approvedStudentIds.isEmpty()) {
            throw new ProjectRuleViolationException("O projeto precisa de estudantes selecionados.");
        }
        List<Student> members = students.findAllById(approvedStudentIds);
        if (members.size() != approvedStudentIds.size()) {
            throw new ResourceNotFoundException("Um ou mais estudantes aprovados não foram encontrados.");
        }
        Professor advisor = professors.findById(opportunity.professorId())
                .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado."));

        Project project = mapper.toEntity(
                opportunity.title(),
                dto.startDate(),
                List.of(advisor),
                members
        );
        Project saved = repository.save(project);
        opportunities.finishSelection(opportunityId);
        return mapper.toResponse(saved);
    }

    private Project findProject(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado."));
    }

}
