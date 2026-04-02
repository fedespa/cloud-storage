package com.fededev.cloudstorage.workspace.invitations.listener;

import com.fededev.cloudstorage.workspace.invitations.event.InvitationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvitationEventListener {

    // private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvitationCreated(InvitationCreatedEvent event) {
        log.info("Enviando correo de invitación para: {}", event.email());
        log.info("TOKEN: {}", event.token());
        try {
            // this.mailService.sendInvitationEmail(event);
        } catch (Exception e) {
            log.error("Error enviando email de invitación", e);
            // lógica de reintento
        }
    }

}
