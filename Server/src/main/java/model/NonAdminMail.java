package model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;

@Component
public class NonAdminMail {
    final private String sender;//sample mail
    final private Session session;
    List<String> recipient;
    private String userName;

    public NonAdminMail(@Value("${sender}") String sender, @Value("${password}") String password,
                        @Value("${host}") String host) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        };
        this.session = Session.getInstance(props, auth);
        this.sender = sender;

    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRecipient(List<String> recipient) {
        this.recipient = recipient;
    }

    public void sendMail() {
        try {
            MimeMessage message = new MimeMessage(session);

            // Set From Field: adding senders email to from field.
            message.setFrom(new InternetAddress(sender));

            // Set To Field: adding recipient's email to from field.
            recipient.forEach(s ->
            {
                try {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(s));
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });

            // Set Subject: subject of the email
            message.setSubject("MyWorkbench User {" + userName + "} Requesting Reset Password");

            BodyPart messageBodyPart = new MimeBodyPart();
            // set body of the email.
            messageBodyPart.setContent("<h3>" + userName + " is requesting password change</h3>" +
                    "<br> Please verify that " + userName + " is a valid person before changing password and do contact" +
                    " that person to <b>verify the password reset was requested from him/her</b>.", "text/html");


            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            // Send email.
            Transport.send(message);
            System.out.println("Mail successfully sent");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
