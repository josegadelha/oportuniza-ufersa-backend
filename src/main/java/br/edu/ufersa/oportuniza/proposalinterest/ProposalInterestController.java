package br.edu.ufersa.oportuniza.proposalinterest;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.edu.ufersa.oportuniza.proposalinterest.dto.ProposalInterestPatch;
import br.edu.ufersa.oportuniza.proposalinterest.dto.ProposalInterestResponse;
import br.edu.ufersa.oportuniza.proposalinterest.dto.ProposalInterestUpdate;
import br.edu.ufersa.oportuniza.auth.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Validated
public class ProposalInterestController {

    private final ProposalInterestApplicationService service;

    public ProposalInterestController(
            ProposalInterestApplicationService service
    ) {
        this.service = service;
    }

    @GetMapping("/proposal-interests")
    public ResponseEntity<List<ProposalInterestResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(service.listMine(principal.getUser()));
    }

    @GetMapping("/project-proposals/{projectProposalId}/interests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ProposalInterestResponse>> listByProjectProposal(
            @PathVariable Long projectProposalId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(
                service.listByProjectProposal(projectProposalId, principal.getUser())
        );
    }

    @GetMapping("/proposal-interests/{proposalInterestId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
    public ResponseEntity<ProposalInterestResponse> findById(
            @PathVariable Long proposalInterestId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(
                service.findById(proposalInterestId, principal.getUser())
        );
    }

    @PostMapping("/project-proposals/{projectProposalId}/interests")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
    public ResponseEntity<ProposalInterestResponse> createForProjectProposal(
            @PathVariable Long projectProposalId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            UriComponentsBuilder uriBuilder
    ) {
        ProposalInterestResponse response =
                service.createForProjectProposal(
                        projectProposalId,
                        principal.getUser()
                );

        URI uri = uriBuilder
                .path("/api/v1/proposal-interests/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/proposal-interests/{proposalInterestId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProposalInterestResponse> update(
            @PathVariable Long proposalInterestId,
            @RequestBody @Valid ProposalInterestUpdate dto,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(
                service.update(proposalInterestId, dto, principal.getUser())
        );
    }

    @PatchMapping("/proposal-interests/{proposalInterestId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProposalInterestResponse> partialUpdate(
            @PathVariable Long proposalInterestId,
            @RequestBody @Valid ProposalInterestPatch dto,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(
                service.partialUpdate(proposalInterestId, dto, principal.getUser())
        );
    }

    @DeleteMapping("/proposal-interests/{proposalInterestId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
    public ResponseEntity<Void> remove(
            @PathVariable Long proposalInterestId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        service.remove(proposalInterestId, principal.getUser());

        return ResponseEntity.noContent().build();
    }
}