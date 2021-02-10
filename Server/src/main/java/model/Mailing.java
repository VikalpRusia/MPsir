package model;

import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Component
public class Mailing {
    final String sender = "18bcs163@ietdavv.edu.in";
    final String host = "smtp.gmail.com";
    final String password = "tjgksuwpvlhilfna";
    final Properties props = System.getProperties();
    final Session session;
    String recipient;

    public Mailing() {
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
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void sendMail() {
        try {
            MimeMessage message = new MimeMessage(session);

            // Set From Field: adding senders email to from field.
            message.setFrom(new InternetAddress(sender));

            // Set To Field: adding recipient's email to from field.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            // Set Subject: subject of the email
            message.setSubject("MyWorkbench Reset Password");

            // set body of the email.
            message.setContent("<h1>Disclaimer:</h1>" +
                    "<br> This e-mail contains privileged information or information belonging to MySql Workbench and is intended solely for the addressee/s. Access to this email by anyone else is unauthorized. Any copying (whole or partial) or further distribution beyond the original recipient is not intended, and may be unlawful. The recipient acknowledges that MySql Workbench is unable to exercise control or ensure or guarantee the integrity of the contents of the information contained in e-mail transmissions and further acknowledges that any views expressed in this message are those of the individual sender and are not binding on MySql Workbench. E-mails are susceptible to alteration and their integrity cannot be guaranteed. MySql Workbench does not accept any liability for any damages caused on account of this e-mail. If you have received this email in error, please contact the sender and delete the material from your computer.", "text/html");

            // Send email.
            Transport.send(message);
            System.out.println("Mail successfully sent");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
