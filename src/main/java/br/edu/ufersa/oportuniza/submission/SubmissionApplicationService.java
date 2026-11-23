package br.edu.ufersa.oportuniza.submission;

import br.edu.ufersa.oportuniza.deliverable.Deliverable;
import br.edu.ufersa.oportuniza.deliverable.DeliverableRepository;
import br.edu.ufersa.oportuniza.notification.NotificationApplicationService;
import br.edu.ufersa.oportuniza.notification.NotificationType;
import br.edu.ufersa.oportuniza.project.ProjectService;
import br.edu.ufersa.oportuniza.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.oportuniza.student.Student;
import br.edu.ufersa.oportuniza.user.User;
import br.edu.ufersa.oportuniza.submission.dto.SubmissionCreate;
import br.edu.ufersa.oportuniza.submission.dto.SubmissionResponse;
import br.edu.ufersa.oportuniza.submission.dto.SubmissionVersionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubmissionApplicationService {

    private final SubmissionRepository repository;
    private final DeliverableRepository deliverables;
    private final SubmissionService domain;
    private final ProjectService projects;
    private final SubmissionMapper mapper;
    private final NotificationApplicationService notifications;

    public SubmissionApplicationService(SubmissionRepository repository, DeliverableRepository deliverables,
            SubmissionService domain, ProjectService projects,
            SubmissionMapper mapper, NotificationApplicationService notifications) {
        this.repository = repository;
        this.deliverables = deliverables;
        this.domain = domain;
        this.projects = projects;
        this.mapper = mapper;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> listMine(User user) {
        List<Submission> submissions;
        if (user instanceof Student) {
            submissions = repository.findByStudentId(user.getId());
        } else {
            submissions = repository.findAdvisedBy(user.getId());
        }
        return mapper.toResponseList(submissions.stream().distinct().toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> listByDeliverable(Long deliverableId, User user) {
        Deliverable deliverable = findDeliverable(deliverableId);
        projects.requireParticipant(deliverable.getProject(), user);
        if (deliverable.getProject().hasAdvisor(user.getId())) {
            return mapper.toResponseList(repository.findByDeliverableId(deliverableId));
        }
        return repository.findByDeliverableIdAndStudentId(deliverableId, user.getId())
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse findById(Long id, User user) {
        return mapper.toResponse(findReadable(id, user));
    }

    @Transactional(readOnly = true)
    public List<SubmissionVersionResponse> versions(Long id, User user) {
        return mapper.toVersionResponseList(findReadable(id, user).getVersions());
    }

    @Transactional
    public SaveResult send(Long deliverableId, SubmissionCreate dto, User user) {
        Deliverable deliverable = findDeliverable(deliverableId);
        Student student = domain.requireSubmitter(deliverable, user);
        Submission submission = repository.findByDeliverableIdAndStudentId(deliverableId, student.getId())
                .orElse(null);
        boolean created = submission == null;
        if (created) submission = mapper.toEntity(deliverable, student);
        submission.submit(dto.filePath());
        Submission saved = repository.save(submission);
        for (var advisor : deliverable.getProject().getAdvisors()) {
            notifications.notifyUser(
                    advisor,
                    "Nova entrega recebida",
                    "Uma nova entrega foi enviada para o entregável \"" + deliverable.getTitle() + "\".",
                    NotificationType.SUBMISSION_RECEIVED
            );
        }
        return new SaveResult(mapper.toResponse(saved), created);
    }

    @Transactional
    public SubmissionResponse approve(Long id, User user) {
        return review(id, user, SubmissionStatus.APPROVED);
    }

    @Transactional
    public SubmissionResponse reject(Long id, User user) {
        return review(id, user, SubmissionStatus.REJECTED);
    }

    public record SaveResult(SubmissionResponse response, boolean created) { }

    private SubmissionResponse review(Long id, User user, SubmissionStatus status) {
        Submission submission = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submissão não encontrada."));
        domain.requireReviewer(submission, user);
        submission.updateReview(status);
        Submission saved = repository.save(submission);

        String decision = status == SubmissionStatus.APPROVED ? "aprovada" : "rejeitada";
        notifications.notifyUser(
                submission.getStudent(),
                "Entrega " + decision,
                "Sua entrega \"" + submission.getDeliverable().getTitle() + "\" foi " + decision + ".",
                NotificationType.SUBMISSION_REVIEW
        );
        return mapper.toResponse(saved);
    }

    private Submission findReadable(Long id, User user) {
        Submission submission = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submissão não encontrada."));
        domain.requireReader(submission, user);
        return submission;
    }

    private Deliverable findDeliverable(Long id) {
        return deliverables.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega não encontrada."));
    }
}
