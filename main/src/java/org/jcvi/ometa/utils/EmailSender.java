/*
 * Copyright J. Craig Venter Institute, 2013
 *
 * The creation of this program was supported by J. Craig Venter Institute
 * and National Institute for Allergy and Infectious Diseases (NIAID),
 * Contract number HHSN272200900007C.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jcvi.ometa.utils;

import org.apache.log4j.Logger;
import org.jtc.common.util.property.PropertyHelper;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 10/17/11
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailSender {
    private Logger logger = Logger.getLogger( EmailSender.class );

    private final String EMAIL_HOST;
    private final String EMAIL_USER;
    private final String EMAIL_PASS;
    private final String EMAIL_FROM;
    private final String EMAIL_BCC;

    public EmailSender() {
        Properties props = PropertyHelper.getHostnameProperties( Constants.PROPERTIES_FILE_NAME );
        this.EMAIL_HOST = props.getProperty(Constants.CONFIG_SMTP_HOST);
        this.EMAIL_USER = props.getProperty(Constants.CONFIG_SMTP_USER);
        this.EMAIL_PASS = props.getProperty(Constants.CONFIG_SMTP_PASSWD);
        this.EMAIL_FROM = props.getProperty(Constants.CONFIG_SMTP_FROM);
        this.EMAIL_BCC = props.getProperty(Constants.CONFIG_SMTP_BCC);
    }

    public void send(String to, String subject, String body, List<String> files) throws Exception {
        Properties mailProps = System.getProperties();
        mailProps.setProperty(this.EMAIL_HOST, this.EMAIL_HOST);
        /*mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.ssl.enable", "true");
        mailProps.put("mail.smtp.port", 587);
        mailProps.put("mail.transport.protocol", "smtp");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.starttls.required", "true");*/

        Session session = Session.getDefaultInstance(mailProps);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(this.EMAIL_FROM));

        if(this.EMAIL_BCC != null && this.EMAIL_BCC.length() > 0) {
            msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(this.EMAIL_BCC));
        }
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body, "UTF-8", "html");
        multipart.addBodyPart(messageBodyPart);

        if(files != null && files.size() > 0) {
            for(String filePath : files) {
                File attachment = new File(filePath);

                if(attachment.exists() && attachment.isFile()) {
                    messageBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(filePath);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(attachment.getName());
                    multipart.addBodyPart(messageBodyPart);
                }
            }
        }
        msg.setContent(multipart);

        //Transport transport = session.getTransport();

        try {
            //transport.connect(this.EMAIL_HOST, 587, this.EMAIL_USER, this.EMAIL_PASS);
            Transport.send(msg);

            //transport.sendMessage(msg, msg.getAllRecipients());
        } catch (Exception ex) {
            System.out.println("Error in sending a message: " + ex.getMessage());
        } finally {
            // Close and terminate the connection.
            //transport.close();
        }
    }
}