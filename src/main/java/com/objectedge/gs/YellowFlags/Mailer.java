package com.objectedge.gs.YellowFlags;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mailer {

    private final String USERNAME = "gustavo.farias@objectedge.com";
    private final String PASSWORD = "qxovidbtgwgjwkjp";

    private Properties props = new Properties(){{
        put("mail.smtp.host", "smtp.gmail.com");
        put("mail.smtp.socketFactory.port", "465");
        put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        put("mail.smtp.auth", "true");
        put("mail.smtp.port", "465");
    }};

    private Session getSession() {
        return Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });
    }

    public void send(String recipients, String subject, String content) {
        try {
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress("yellow-flags@objectedge.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipients));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}