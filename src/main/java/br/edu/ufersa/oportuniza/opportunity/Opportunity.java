package br.edu.ufersa.oportuniza.opportunity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.ufersa.oportuniza.professor.Professor;
import br.edu.ufersa.oportuniza.shared.exception.BusinessValidation;
import br.edu.ufersa.oportuniza.shared.exception.BusinessRuleViolationException;
import br.edu.ufersa.oportuniza.shared.exception.InvalidBusinessDataException;
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
    name = "opportunities",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"professor_id", "title"}
    )
)
class Opportunity {

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
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OpportunityType type;

    @Column(name = "positions", nullable = false)
    private Integer positions;

    @Column(name = "workload_hours", nullable = false)
    private Integer workloadHours;

    @Column(name = "remuneration", nullable = false)
    private Double remuneration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OpportunityStatus status;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @ElementCollection
    @CollectionTable(
        name = "opportunity_requirements",
        joinColumns = @JoinColumn(name = "opportunity_id")
    )
    @Column(name = "requirement", nullable = false)
    private List<String> requirements = new ArrayList<>();

    protected Opportunity() {
    }

    private Opportunity(Builder builder) {
        this.id = builder.id;
        this.title = requireText(builder.title, "O título é obrigatório!");
        this.description = requireText(builder.description, "A descrição é obrigatória!");
        this.professor = builder.professor;
        this.type = builder.type;
        this.positions = builder.positions;
        this.workloadHours = builder.workloadHours;
        this.remuneration = builder.remuneration;
        this.status = OpportunityStatus.DRAFT;
        this.applicationDeadline = builder.applicationDeadline;
        this.requirements = new ArrayList<>(builder.requirements);
    }

    public boolean isRemunerated() {
        return remuneration > 0.0;
    }

    public void publish() {
        if (status != OpportunityStatus.DRAFT) {
            throw new BusinessRuleViolationException("Somente oportunidades em rascunho podem ser publicadas.");
        }
        this.status = OpportunityStatus.OPEN;
        this.publishedAt = LocalDateTime.now();
    }

    public void close() {
        if (status != OpportunityStatus.OPEN) {
            throw new BusinessRuleViolationException("Somente oportunidades abertas podem ser encerradas.");
        }
        this.status = OpportunityStatus.CLOSED;
    }

    public void finish() {
        if (!status.canTransitionTo(OpportunityStatus.FINISHED)) {
            throw new BusinessRuleViolationException(
                    "Somente oportunidades abertas ou encerradas podem concluir a seleção."
            );
        }
        this.status = OpportunityStatus.FINISHED;
    }

    public void updateStatus(OpportunityStatus nextStatus) {
        OpportunityStatus status = BusinessValidation.requireNonNull(
                nextStatus,
                "O status da oportunidade é obrigatório."
        );
        if (this.status == status) {
            return;
        }
        if (status == OpportunityStatus.FINISHED) {
            throw new BusinessRuleViolationException(
                    "Finalize a seleção pela ação de criação do projeto."
            );
        }
        if (!this.status.canTransitionTo(status)) {
            throw new BusinessRuleViolationException("Transição de status inválida para a oportunidade.");
        }
        switch (status) {
            case OPEN -> publish();
            case CLOSED -> close();
            case DRAFT, FINISHED -> throw new BusinessRuleViolationException(
                    "Transição de status inválida para a oportunidade."
            );
        }
    }

    public void updateDetails(String title, String description, OpportunityType type,
                              Integer positions, Integer workloadHours, Double remuneration,
                              LocalDate applicationDeadline, List<String> requirements) {
        if (status == OpportunityStatus.FINISHED) {
            throw new BusinessRuleViolationException("Oportunidades finalizadas não podem ser alteradas.");
        }
        String updatedTitle = requireText(title, "O título é obrigatório!");
        String updatedDescription = requireText(description, "A descrição é obrigatória!");
        OpportunityType updatedType = BusinessValidation.requireNonNull(
                type,
                "O tipo da oportunidade é obrigatório!"
        );
        Builder.validatePositions(positions);
        Builder.validateWorkloadHours(workloadHours);
        Builder.validateRemuneration(remuneration);
        this.title = updatedTitle;
        this.description = updatedDescription;
        this.type = updatedType;
        this.positions = positions;
        this.workloadHours = workloadHours;
        this.remuneration = remuneration;
        this.applicationDeadline = applicationDeadline;
        this.requirements.clear();
        if (requirements != null) {
            this.requirements.addAll(requirements);
        }
    }

    public Professor getProfessor() {
        return professor;
    }

    public OpportunityType getType() {
        return type;
    }

    public Integer getPositions() {
        return positions;
    }

    public Integer getWorkloadHours() {
        return workloadHours;
    }

    public Double getRemuneration() {
        return remuneration;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public List<String> getRequirements() {
        return List.copyOf(requirements);
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

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidBusinessDataException(message);
        }
        return value;
    }

    public static class Builder {

        private Long id;

        private final Professor professor;
        private final String title;
        private final String description;
        private final OpportunityType type;

        private Integer positions = 1;
        private Integer workloadHours = 0;
        private Double remuneration = 0.0;
        private LocalDate applicationDeadline;
        private List<String> requirements = new ArrayList<>();

        public Builder(
                Professor professor,
                String title,
                String description,
                OpportunityType type
        ) {
            this.professor = BusinessValidation.requireNonNull(professor, "O professor é obrigatório!");
            this.title = title;
            this.description = description;
            this.type = BusinessValidation.requireNonNull(type, "O tipo da oportunidade é obrigatório!");
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPositions(Integer positions) {
            validatePositions(positions);
            this.positions = positions;
            return this;
        }

        public Builder withWorkloadHours(Integer workloadHours) {
            validateWorkloadHours(workloadHours);
            this.workloadHours = workloadHours;
            return this;
        }

        public Builder withRemuneration(Double remuneration) {
            validateRemuneration(remuneration);
            this.remuneration = remuneration;
            return this;
        }

        public Builder withApplicationDeadline(
                LocalDate applicationDeadline
        ) {
            this.applicationDeadline = applicationDeadline;
            return this;
        }

        public Builder withRequirements(List<String> requirements) {
            this.requirements =
                    requirements == null
                            ? new ArrayList<>()
                            : new ArrayList<>(requirements);

            return this;
        }

        public Opportunity build() {
            validatePositions(positions);
            validateWorkloadHours(workloadHours);
            validateRemuneration(remuneration);
            return new Opportunity(this);
        }

        private static void validatePositions(Integer positions) {
            if (positions == null || positions < 1) {
                throw new InvalidBusinessDataException("A quantidade de vagas deve ser maior que zero!");
            }
        }

        private static void validateWorkloadHours(Integer workloadHours) {
            if (workloadHours == null || workloadHours < 0) {
                throw new InvalidBusinessDataException("A carga horária não pode ser negativa!");
            }
        }

        private static void validateRemuneration(Double remuneration) {
            if (remuneration == null || remuneration < 0.0) {
                throw new InvalidBusinessDataException("A remuneração não pode ser negativa!");
            }
        }
    }
}
