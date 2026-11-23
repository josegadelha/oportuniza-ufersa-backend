package br.edu.ufersa.oportuniza.opportunity;

public enum OpportunityStatus {
    DRAFT, OPEN, CLOSED, FINISHED;

    public boolean canTransitionTo(OpportunityStatus nextStatus) {
        return switch (this) {
            case DRAFT -> nextStatus == OPEN;
            case OPEN -> nextStatus == CLOSED || nextStatus == FINISHED;
            case CLOSED -> nextStatus == FINISHED;
            case FINISHED -> false;
        };
    }
}
