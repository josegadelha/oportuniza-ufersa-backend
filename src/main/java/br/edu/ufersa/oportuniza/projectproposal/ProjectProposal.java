package br.edu.ufersa.oportuniza.projectproposal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.ufersa.oportuniza.proposal.ProposalStatus;
import br.edu.ufersa.oportuniza.shared.exception.BusinessRuleViolationException;
import br.edu.ufersa.oportuniza.shared.exception.BusinessValidation;
import br.edu.ufersa.oportuniza.shared.exception.InvalidBusinessDataException;
import br.edu.ufersa.oportuniza.student.Student;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "project_proposals",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"student_id", "title"}
    )
)
class ProjectProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProposalStatus status;

    @ElementCollection
    @CollectionTable(
            name = "project_proposal_desired_skills",
            joinColumns = @JoinColumn(name = "project_proposal_id")
    )
    @Column(name = "desired_skill", nullable = false)
    private List<String> desiredSkills = new ArrayList<>();

    protected ProjectProposal() {
    }

    private ProjectProposal(Builder builder) {
        this.title = requireText(
                builder.title,
                "O título é obrigatório!"
        );
        this.description = requireText(
                builder.description,
                "A descrição é obrigatória!"
        );
        this.publishedAt = LocalDateTime.now();
        this.student = builder.student;
        this.status = ProposalStatus.OPEN;
        this.desiredSkills = new ArrayList<>(builder.desiredSkills);
    }

    public void updateStatus(ProposalStatus status) {
        ProposalStatus nextStatus = BusinessValidation.requireNonNull(
                status,
                "O status da proposta é obrigatório!"
        );
        if (this.status == nextStatus) {
            return;
        }
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new BusinessRuleViolationException("Transição de status inválida para a proposta.");
        }
        this.status = nextStatus;
    }

    public void updateDetails(String title, String description, List<String> desiredSkills) {
        if (status == ProposalStatus.CLOSED) {
            throw new BusinessRuleViolationException("Propostas encerradas não podem ser alteradas.");
        }
        String updatedTitle = requireText(title, "O título é obrigatório!");
        String updatedDescription = requireText(description, "A descrição é obrigatória!");
        this.title = updatedTitle;
        this.description = updatedDescription;
        this.desiredSkills.clear();
        if (desiredSkills != null) {
            this.desiredSkills.addAll(desiredSkills);
        }
    }

    public Student getStudent() {
        return student;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public List<String> getDesiredSkills() {
        return List.copyOf(desiredSkills);
    }

    private static String requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessDataException(message);
        }

        return value;
    }

    static class Builder {

        private final Student student;
        private final String title;
        private final String description;

        private List<String> desiredSkills = new ArrayList<>();

        Builder(
                Student student,
                String title,
                String description
        ) {
            this.student = BusinessValidation.requireNonNull(
                    student,
                    "O estudante é obrigatório!"
            );

            this.title = title;
            this.description = description;
        }

        Builder withDesiredSkills(List<String> desiredSkills) {
            this.desiredSkills =
                    desiredSkills == null
                            ? new ArrayList<>()
                            : new ArrayList<>(desiredSkills);

            return this;
        }

        ProjectProposal build() {
            return new ProjectProposal(this);
        }
    }
}