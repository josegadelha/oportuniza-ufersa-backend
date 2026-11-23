package br.edu.ufersa.oportuniza.projectproposal;

import org.springframework.stereotype.Service;

import br.edu.ufersa.oportuniza.student.Student;
import br.edu.ufersa.oportuniza.shared.exception.ResourceAccessDeniedException;
import br.edu.ufersa.oportuniza.user.User;

@Service
class ProjectProposalService {

    public Student requireOwner(ProjectProposal projectProposal, User actor) {
        if (!(actor instanceof Student student)) {
            throw new ResourceAccessDeniedException("Somente o estudante dono da proposta pode realizar esta operação.");
        }

        if (!projectProposal.getStudent().getId().equals(student.getId())) {
            throw new ResourceAccessDeniedException("Você não é o dono desta proposta de projeto.");
        }

        return student;
    }

    public Student requireStudent(User actor) {
        if (!(actor instanceof Student student)) {
            throw new ResourceAccessDeniedException("Somente estudantes podem criar ou listar propostas próprias.");
        }
        return student;
    }
}
