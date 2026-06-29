package com.puntodeapoyo.notifications;

import java.util.ArrayList;
import java.util.List;

import com.puntodeapoyo.inspectioncases.events.CaseAssignedEvent;
import com.puntodeapoyo.inspectioncases.events.CaseCreatedEvent;
import com.puntodeapoyo.inspectioncases.events.CaseStatusChangedEvent;
import com.puntodeapoyo.inspectioncases.model.InspectionCase;
import com.puntodeapoyo.inspectioncases.repository.InspectionCaseRepository;
import com.puntodeapoyo.users.model.InternalUser;
import com.puntodeapoyo.users.repository.InternalUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InspectionCaseEmailNotificationListener {

    private final EmailNotificationService emailNotificationService;
    private final EmailNotificationProperties emailProperties;
    private final InspectionCaseRepository inspectionCaseRepository;
    private final InternalUserRepository userRepository;

    public InspectionCaseEmailNotificationListener(
            EmailNotificationService emailNotificationService,
            EmailNotificationProperties emailProperties,
            InspectionCaseRepository inspectionCaseRepository,
            InternalUserRepository userRepository
    ) {
        this.emailNotificationService = emailNotificationService;
        this.emailProperties = emailProperties;
        this.inspectionCaseRepository = inspectionCaseRepository;
        this.userRepository = userRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCaseCreated(CaseCreatedEvent event) {
        inspectionCaseRepository.findById(event.caseId()).ifPresent(inspectionCase -> {
            List<String> recipients = new ArrayList<>(internalRecipients());
            addIfPresent(recipients, inspectionCase.applicantEmail());

            emailNotificationService.send(
                    recipients,
                    "Caso creado " + inspectionCase.trackingCode(),
                    """
                    Se ha creado un nuevo caso.

                    Codigo: %s
                    Solicitante: %s
                    Telefono: %s
                    Direccion: %s
                    Ciudad: %s
                    Estado: %s
                    Prioridad: %s
                    """.formatted(
                            inspectionCase.trackingCode(),
                            inspectionCase.applicantName(),
                            inspectionCase.applicantPhone(),
                            inspectionCase.address(),
                            nullToDash(inspectionCase.city()),
                            inspectionCase.status(),
                            inspectionCase.priority()
                    )
            );
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCaseAssigned(CaseAssignedEvent event) {
        inspectionCaseRepository.findById(event.caseId()).ifPresent(inspectionCase -> {
            List<String> recipients = new ArrayList<>(internalRecipients());
            for (Long engineerId : event.engineerIds()) {
                userRepository.findById(engineerId)
                        .map(InternalUser::email)
                        .ifPresent(email -> addIfPresent(recipients, email));
            }

            emailNotificationService.send(
                    recipients,
                    "Caso asignado " + inspectionCase.trackingCode(),
                    """
                    Se ha asignado un caso.

                    Codigo: %s
                    Ingenieros asignados: %s
                    Direccion: %s
                    Ciudad: %s
                    Prioridad: %s
                    Estado actual: %s
                    """.formatted(
                            inspectionCase.trackingCode(),
                            event.engineerIds(),
                            inspectionCase.address(),
                            nullToDash(inspectionCase.city()),
                            inspectionCase.priority(),
                            inspectionCase.status()
                    )
            );
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCaseStatusChanged(CaseStatusChangedEvent event) {
        inspectionCaseRepository.findById(event.caseId()).ifPresent(inspectionCase -> {
            List<String> recipients = new ArrayList<>(internalRecipients());
            addIfPresent(recipients, inspectionCase.applicantEmail());

            emailNotificationService.send(
                    recipients,
                    "Estado actualizado " + inspectionCase.trackingCode(),
                    """
                    El estado de un caso ha cambiado.

                    Codigo: %s
                    Estado anterior: %s
                    Estado nuevo: %s
                    Solicitante: %s
                    Direccion: %s
                    Ciudad: %s
                    """.formatted(
                            inspectionCase.trackingCode(),
                            event.previousStatus(),
                            event.newStatus(),
                            inspectionCase.applicantName(),
                            inspectionCase.address(),
                            nullToDash(inspectionCase.city())
                    )
            );
        });
    }

    private List<String> internalRecipients() {
        if (emailProperties.internalRecipients() == null) {
            return List.of();
        }
        return emailProperties.internalRecipients();
    }

    private void addIfPresent(List<String> recipients, String email) {
        if (email != null && !email.isBlank()) {
            recipients.add(email.trim());
        }
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
