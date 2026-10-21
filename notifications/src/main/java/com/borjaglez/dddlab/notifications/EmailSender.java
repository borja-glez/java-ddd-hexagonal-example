package com.borjaglez.dddlab.notifications;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simula el proveedor de email. Enviar emails es un subdominio genérico: en un sistema real esto
 * sería SES o SendGrid, comprado y no construido. Aquí escribe en el log y recuerda lo enviado.
 */
@Component
public class EmailSender {

  private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

  public record Email(String to, String subject, String body) {}

  private final List<Email> sent = Collections.synchronizedList(new ArrayList<>());

  void send(String to, String subject, String body) {
    Email email = new Email(to, subject, body);
    sent.add(email);
    log.info("Email para {}: [{}] {}", to, subject, body);
  }

  public List<Email> sentEmails() {
    synchronized (sent) {
      return List.copyOf(sent);
    }
  }
}
