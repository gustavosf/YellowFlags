package com.objectedge.gs.YellowFlags;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@SuppressWarnings("ConstantConditions")
public class Mailer {

    private Properties props;

    public Mailer() {
        props = new Properties(){{
            put("mail.smtp.host", Config.get("smtp.host", "mailer"));
            put("mail.smtp.socketFactory.port", Config.get("smtp.port", "mailer"));
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            put("mail.smtp.auth", "true");
            put("mail.smtp.port", Config.get("smtp.port", "mailer"));
        }};
    }

    private Session getSession() {
        return Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                (String)Config.get("username", "mailer"),
                                (String)Config.get("password", "mailer"));
                    }
                });
    }

    public void send(String recipients, String subject, String content) {
        try {
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress((String)Config.get("from", "mailer")));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipients));
            message.setSubject(subject);
            message.setContent(
                    "<pre style=\"font-size:14px\">"+content+"</pre>",
                    "text/html; charset=utf-8");
            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}