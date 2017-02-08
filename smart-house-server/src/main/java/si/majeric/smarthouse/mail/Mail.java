package si.majeric.smarthouse.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import ch.qos.logback.core.util.OptionHelper;
import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.alarm.MotionDetectionSnapWatch;

/**
 * Created by Uros Majeric on 04/11/16.
 */
public class Mail {
    static final Logger logger = LoggerFactory.getLogger(MotionDetectionSnapWatch.class);

    public static void sendEmail(String[] recepients, String subject, String body, List<Path> attachments) throws MessagingException {
        if (Boolean.valueOf(Environment.getProperty("mail.enabled"))) {
            logger.warn("Mail is not enabled in config");
            return;
        }

        Session session = buildSessionFromProperties();
        MimeMessage mimeMsg = new MimeMessage(session);
        if (Environment.getProperty("mail.from") != null) {
            mimeMsg.setFrom(getAddress(Environment.getProperty("mail.from")));
        }

        Multipart multipart = new MimeMultipart();

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(body);
        multipart.addBodyPart(textBodyPart);

        for (Path path : attachments) {
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(path.toString());
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(path.getFileName().toString());
            multipart.addBodyPart(attachmentBodyPart);
        }

        mimeMsg.setContent(multipart);
        mimeMsg.setSentDate(new Date());

        List<Address> addresses = new ArrayList<>();
        for (String to : recepients) {
            final InternetAddress address = getAddress(to);
            addresses.add(address);
        }
        mimeMsg.setRecipients(Message.RecipientType.TO, addresses.toArray(new Address[addresses.size()]));
        mimeMsg.setSubject(subject);

        Transport.send(mimeMsg);
    }

    private static Session buildSessionFromProperties() {
        Properties props = new Properties(OptionHelper.getSystemProperties());
        final Map<String, String> mailPropertyMap = Environment.getPropertyMap("mail");
        if (mailPropertyMap.get("smtpHost") != null) {
            props.put("mail.smtp.host", mailPropertyMap.get("smtpHost"));
        }
        props.put("mail.smtp.port", mailPropertyMap.get("smtpPort"));

        if (mailPropertyMap.get("localhost") != null) {
            props.put("mail.smtp.localhost", mailPropertyMap.get("localhost"));
        }

        LoginAuthenticator loginAuthenticator = null;

        if (mailPropertyMap.get("username") != null) {
            loginAuthenticator = new LoginAuthenticator(mailPropertyMap.get("username"), mailPropertyMap.get("password"));
            props.put("mail.smtp.auth", "true");
        }

        final boolean isSTARTTLS = Boolean.parseBoolean(mailPropertyMap.get("isSTARTTLS"));
        final boolean isSSL = Boolean.parseBoolean(mailPropertyMap.get("isSSL"));
        if (isSTARTTLS && isSSL) {
            logger.error("Both SSL and StartTLS cannot be enabled simultaneously");
        } else {
            if (isSTARTTLS) {
                // see also http://jira.qos.ch/browse/LBCORE-225
                props.put("mail.smtp.starttls.enable", "true");
            }
            if (isSSL) {
                String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
                props.put("mail.smtp.socketFactory.port", mailPropertyMap.get("smtpPort"));
                props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
                props.put("mail.smtp.socketFactory.fallback", "true");
            }
        }
        // props.put("mail.debug", "true");

        return Session.getInstance(props, loginAuthenticator);
    }

    private static InternetAddress getAddress(String addressStr) {
        try {
            return new InternetAddress(addressStr);
        } catch (AddressException e) {
            logger.error("Could not parse address [" + addressStr + "].", e);
            return null;
        }
    }

    public static class LoginAuthenticator extends Authenticator {
        String username;
        String password;

        LoginAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }

    }
}
