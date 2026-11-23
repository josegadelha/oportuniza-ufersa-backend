package br.edu.ufersa.oportuniza.candidacy;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import br.edu.ufersa.oportuniza.auth.AuthenticatedUser;
import br.edu.ufersa.oportuniza.candidacy.dto.CandidacyPatch;
import br.edu.ufersa.oportuniza.candidacy.dto.CandidacyResponse;
import br.edu.ufersa.oportuniza.candidacy.dto.CandidacyUpdate;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Validated
public class CandidacyController {

    private final CandidacyApplicationService service;

    public CandidacyController(CandidacyApplicationService service) {
        this.service = service;
    }

    @GetMapping("/candidacies")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CandidacyResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(service.listByStudent(principal.getUser()));
    }

    @GetMapping("/opportunities/{opportunityId}/candidacies")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<CandidacyResponse>> listByOpportunity(
            @PathVariable Long opportunityId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(service.listByOpportunity(opportunityId, principal.getUser()));
    }

    @GetMapping("/students/candidacies")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CandidacyResponse>> listByStudent(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(service.listByStudent(principal.getUser()));
    }

    @GetMapping("/candidacies/{candidacyId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
    public ResponseEntity<CandidacyResponse> findById(
            @PathVariable Long candidacyId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(service.findById(candidacyId, principal.getUser()));
    }

    @PostMapping("/opportunities/{opportunityId}/candidacies")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CandidacyResponse> createForOpportunity(
            @PathVariable Long opportunityId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            UriComponentsBuilder uriBuilder
    ) {
        CandidacyResponse response = service.createForOpportunity(opportunityId, principal.getUser());

        URI uri = uriBuilder.path("/api/v1/candidacies/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/candidacies/{candidacyId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<CandidacyResponse> update(
            @PathVariable Long candidacyId,
            @RequestBody @Valid CandidacyUpdate dto,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(service.update(candidacyId, dto, principal.getUser()));
    }

    @PatchMapping("/candidacies/{candidacyId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<CandidacyResponse> partialUpdate(
            @PathVariable Long candidacyId,
            @RequestBody @Valid CandidacyPatch dto,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return ResponseEntity.ok(service.partialUpdate(candidacyId, dto, principal.getUser()));
    }

    @DeleteMapping("/candidacies/{candidacyId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
    public ResponseEntity<Void> remove(
            @PathVariable Long candidacyId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        service.remove(candidacyId, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
