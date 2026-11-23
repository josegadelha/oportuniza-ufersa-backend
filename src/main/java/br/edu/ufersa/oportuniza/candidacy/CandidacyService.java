package br.edu.ufersa.oportuniza.candidacy;

import org.springframework.stereotype.Service;

import br.edu.ufersa.oportuniza.opportunity.OpportunityInternalApi;
import br.edu.ufersa.oportuniza.opportunity.OpportunitySummary;
import br.edu.ufersa.oportuniza.professor.Professor;
import br.edu.ufersa.oportuniza.student.Student;
import br.edu.ufersa.oportuniza.shared.exception.ResourceAccessDeniedException;
import br.edu.ufersa.oportuniza.user.User;

@Service
class CandidacyService {

    private final OpportunityInternalApi opportunityApi;

    CandidacyService(OpportunityInternalApi opportunityApi) {
        this.opportunityApi = opportunityApi;
    }

    public void requireReader(Candidacy candidacy, User actor) {
        if (candidacy.getStudent().getId().equals(actor.getId())) {
            return;
        }

        requireOpportunityOwner(opportunityApi.findById(candidacy.getOpportunityId()), actor);
    }

    public void requireOpportunityOwner(OpportunitySummary opportunity, User actor) {
        if (!(actor instanceof Professor)
                || !opportunity.professorId().equals(actor.getId())) {
            throw new ResourceAccessDeniedException("Somente o professor responsável pode avaliar esta candidatura.");
        }
    }

    public Student requireStudent(User actor) {
        if (!(actor instanceof Student student)) {
            throw new ResourceAccessDeniedException("Somente estudantes podem acessar candidaturas próprias.");
        }
        return student;
    }
}
