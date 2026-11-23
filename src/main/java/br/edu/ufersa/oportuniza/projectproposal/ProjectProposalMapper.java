package br.edu.ufersa.oportuniza.projectproposal;

import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.ufersa.oportuniza.projectproposal.dto.ProjectProposalCreate;
import br.edu.ufersa.oportuniza.projectproposal.dto.ProjectProposalResponse;
import br.edu.ufersa.oportuniza.student.Student;

@Component
class ProjectProposalMapper {

    public ProjectProposal toEntity(Student student, ProjectProposalCreate dto) {
        if (dto == null) {
            return null;
        }

        return new ProjectProposal.Builder(student, dto.title(), dto.description())
                .withDesiredSkills(dto.desiredSkills())
                .build();
    }

    public ProjectProposalResponse toResponse(ProjectProposal projectProposal) {
        if (projectProposal == null) {
            return null;
        }

        return new ProjectProposalResponse(
                projectProposal.getId(),
                projectProposal.getStudent().getId(),
                projectProposal.getTitle(),
                projectProposal.getDescription(),
                projectProposal.getStatus(),
                projectProposal.getPublishedAt(),
                projectProposal.getDesiredSkills()
        );
    }

    public List<ProjectProposalResponse> toResponseList(List<ProjectProposal> projectProposals) {
        if (projectProposals == null) {
            return null;
        }

        return projectProposals.stream().map(this::toResponse).toList();
    }
}
