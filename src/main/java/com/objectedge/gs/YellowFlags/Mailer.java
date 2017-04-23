package com.objectedge.gs.YellowFlags;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Mailer {

    public static Properties config;
    static {
        config = new Properties();
        InputStream is = Mailer.class.getResourceAsStream("/mailer.properties");
        try {
            config.load(is);
        } catch (IOException e) {
            Log.info("[Mailer] Unable to load config for mailer");
        }
    }

    private Properties props = new Properties(){{
        put("mail.smtp.host", "smtp.gmail.com");
        put("mail.smtp.socketFactory.port", "465");
        put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        put("mail.smtp.auth", "true");
        put("mail.smtp.port", "465");
    }};

    public String getConfig(String param) {
        return config.getProperty(param);
    }

    private Session getSession() {
        return Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                getConfig("username"), getConfig("password"));
                    }
                });
    }

    public void send(String recipients, String subject, String content) {
        try {
            MimeMessage message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(getConfig("from")));
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