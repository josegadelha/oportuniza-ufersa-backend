package br.edu.ufersa.oportuniza.proposal;

public enum ProposalStatus {
    OPEN, IN_PROGRESS, CLOSED;

    public boolean canTransitionTo(ProposalStatus nextStatus) {
        return switch (this) {
            case OPEN -> nextStatus == IN_PROGRESS || nextStatus == CLOSED;
            case IN_PROGRESS -> nextStatus == CLOSED;
            case CLOSED -> false;
        };
    }
}
