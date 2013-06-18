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

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
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

    protected static final String EMAIL_ETL_LIST_PROP = Constants.CONFIGURATION_PREFIX+".mail_etl_to_address";
    protected static final String EMAIL_HELP_LIST_PROP = Constants.CONFIGURATION_PREFIX+".mail_help_to_address";
    protected static final String EMAIL_JSON_LIST_PROP = Constants.CONFIGURATION_PREFIX+".mail_json_to_address";
    protected static final String EMAIL_SERVER_HOST_PROP = Constants.CONFIGURATION_PREFIX+".mail_host";
    protected static final String EMAIL_AUTH_USER_PROP = Constants.CONFIGURATION_PREFIX+".mail_auth_user";
    protected static final String EMAIL_AUTH_PASSWD_PROP = Constants.CONFIGURATION_PREFIX+".mail_auth_passwd";
    protected static final String EMAIL_REPLY_TO_PROP = Constants.CONFIGURATION_PREFIX+".mail_reply_to";

    public void send( String type, String subject, String body ) {
        try {
            Properties props = PropertyHelper.getHostnameProperties( Constants.PROPERTIES_FILE_NAME );
            String smtpServer = props.getProperty( EMAIL_SERVER_HOST_PROP, null );

            String authUser = props.getProperty( EMAIL_AUTH_USER_PROP );
            String authPassword = props.getProperty( EMAIL_AUTH_PASSWD_PROP );
            String to = "";
            if( "etl".equals( type ) ) {
                to = props.getProperty( EMAIL_ETL_LIST_PROP );
            } else if("help".equals(type)) {
                to = props.getProperty(EMAIL_HELP_LIST_PROP);
            } else {
                to = props.getProperty( EMAIL_JSON_LIST_PROP );
            }
            int timeout = Integer.parseInt( props.getProperty( Constants.CONFIGURATION_PREFIX+".mail_timeout" ) );
            String replyto = props.getProperty( EMAIL_REPLY_TO_PROP );

            Authenticator authenticator = new Authenticator(authUser, authPassword);
            InternetAddress[] toList  = InternetAddress.parse(to.trim(),false);
            props.put("mail.smtp.host", smtpServer);
            if(timeout > 0) {
                props.put("mail.smtp.timeout",String.valueOf(timeout));
            }
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.port", "25");
            props.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
            Session session = Session.getInstance(props, authenticator);

            // -- Create a new message --
            Message msg = new MimeMessage(session);
            // -- Set the FROM and TO fields --
            msg.setFrom(new InternetAddress(authenticator.getPasswordAuthentication().getUserName()+"@domain.com"));
            msg.setReplyTo(new InternetAddress[]{new InternetAddress(replyto)});
            msg.setRecipients(Message.RecipientType.TO, toList);
            // -- Set the subject and body text --
            msg.setSubject(subject);
            msg.setText(body);
            // -- Set some other header information --
            msg.setHeader("X-Mailer", "LOTONtechEmail");
            msg.setSentDate(new Date());
            // -- Send the message --
            Transport.send(msg);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

class Authenticator extends javax.mail.Authenticator {
    private PasswordAuthentication authentication;

    public Authenticator(String username, String password) {
        authentication = new PasswordAuthentication(username, password);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }
}