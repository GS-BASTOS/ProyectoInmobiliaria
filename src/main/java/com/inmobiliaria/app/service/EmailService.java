package com.inmobiliaria.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.contact.email}")
    private String contactEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── Consulta sobre un inmueble específico ─────────────
    public void enviarConsultaInmueble(String propertyCode,
                                       String nombre,
                                       String telefono,
                                       String email,
                                       String mensaje) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(contactEmail);
        mail.setSubject("Nueva consulta · Inmueble " + propertyCode);
        mail.setText(
            "═══════════════════════════════════\n" +
            "  NUEVA CONSULTA — SOLVIA STORE BILBAO\n" +
            "═══════════════════════════════════\n\n" +
            "Inmueble:   " + propertyCode + "\n\n" +
            "Nombre:     " + nombre + "\n" +
            "Teléfono:   " + telefono + "\n" +
            "Email:      " + (email != null && !email.isBlank() ? email : "No indicado") + "\n\n" +
            "Mensaje:\n"   + (mensaje != null && !mensaje.isBlank() ? mensaje : "Sin mensaje") + "\n\n" +
            "───────────────────────────────────\n" +
            "Enviado desde solviastorebilbao.com"
        );
        mailSender.send(mail);
    }

    // ── Consulta general desde el home ────────────────────
    public void enviarConsultaGeneral(String nombre,
                                      String telefono,
                                      String email,
                                      String motivo,
                                      String mensaje) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(contactEmail);
        mail.setSubject("Nueva consulta general – Solvia Store Bilbao");
        mail.setText(
            "═══════════════════════════════════\n" +
            "  CONSULTA GENERAL — SOLVIA STORE BILBAO\n" +
            "═══════════════════════════════════\n\n" +
            "Nombre:    " + nombre + "\n" +
            "Teléfono:  " + telefono + "\n" +
            "Email:     " + (email != null && !email.isBlank() ? email : "No indicado") + "\n" +
            "Motivo:    " + (motivo != null && !motivo.isBlank() ? motivo : "No indicado") + "\n\n" +
            "Mensaje:\n" + (mensaje != null && !mensaje.isBlank() ? mensaje : "Sin mensaje") + "\n\n" +
            "───────────────────────────────────\n" +
            "Enviado desde solviastorebilbao.com"
        );
        mailSender.send(mail);
    }
}
