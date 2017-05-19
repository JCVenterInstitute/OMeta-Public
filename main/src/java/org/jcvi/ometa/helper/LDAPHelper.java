package org.jcvi.ometa.helper;

import com.novell.ldap.*;
import org.jcvi.ometa.utils.CommonTool;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.EmailSender;
import org.jtc.common.util.property.PropertyHelper;
import sun.misc.BASE64Encoder;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Properties;

/**
 * User: movence
 * Date: 12/8/14
 * Time: 2:57 PM
 * org.jcvi.ometa.helper
 */
public class LDAPHelper {
    private final String LDAP_HOST;
    private final String LDAP_ADMIN;
    private final String LDAP_ADMIN_PASS;
    private final String LDAP_USER_DN;

    public LDAPHelper() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        this.LDAP_HOST = props.getProperty("ometa.ldap.host");
        this.LDAP_ADMIN = props.getProperty("ometa.ldap.adminDN");
        this.LDAP_ADMIN_PASS = props.getProperty("ometa.ldap.adminPass");
        this.LDAP_USER_DN = props.getProperty("ometa.ldap.userDN");
    }

    public boolean resetPassword(String userName, String userFullName, String userMail) throws Exception {
        boolean beenReset = false;

        int ldapPort = LDAPConnection.DEFAULT_PORT;
        int ldapVersion = LDAPConnection.LDAP_V3;

        LDAPConnection lc = new LDAPConnection();

        try {
            lc.connect(this.LDAP_HOST, ldapPort);
            lc.bind(ldapVersion, this.LDAP_ADMIN, this.LDAP_ADMIN_PASS.getBytes("UTF8"));

            String userDN = String.format(this.LDAP_USER_DN, userName);

            String randomPassword = new BigInteger(55, new SecureRandom()).toString(32);

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(randomPassword.getBytes());
            String newBase64 = new BASE64Encoder().encode(digest.digest());

            LDAPModification[] modifications = new LDAPModification[1];
            LDAPAttribute newPassAttribute = new LDAPAttribute("userPassword", "{MD5}" + newBase64);
            modifications[0] = new LDAPModification(LDAPModification.REPLACE, newPassAttribute);
            lc.modify(userDN, modifications);

            beenReset = true;

            lc.disconnect();

            // send mail to the user with a random password
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Dear ").append(userFullName).append(",<br/><br/>");
            bodyBuilder.append( " Your OMETA account password has been reset to ");
            bodyBuilder.append("&nbsp;").append(randomPassword).append("<br/><br/>");
            bodyBuilder.append(" You may use this new password and your existing username to login the OMETA ").append("<br/><br/>");
            bodyBuilder.append(" If this password reset request did not originate from you, please contact the OMETA immediately.");

            bodyBuilder.append("<br/><br/>The OMETA Team<br/>");
            bodyBuilder.append(Constants.DPCC_MAIL_SIGNATURE_HELP);


            EmailSender mailer = new EmailSender();
            mailer.send(userMail, "OMETA - password reset", bodyBuilder.toString(), null);
        } catch(Exception ex) {
            throw ex;
        }

        return beenReset;
    }

    public boolean updatePassword(String userName, String oldPass, String newPass) throws Exception {
        boolean modified = false;

        int ldapPort = LDAPConnection.DEFAULT_PORT;
        int ldapVersion = LDAPConnection.LDAP_V3;

        LDAPConnection lc = new LDAPConnection();

        try {
            lc.connect(this.LDAP_HOST, ldapPort);
            lc.bind(ldapVersion, this.LDAP_ADMIN, this.LDAP_ADMIN_PASS.getBytes("UTF8"));

            String userDN = String.format(this.LDAP_USER_DN, userName);

            MessageDigest digest = MessageDigest.getInstance("MD5");

            digest.update(oldPass.getBytes());
            String oldBase64 = new BASE64Encoder().encode(digest.digest());
            LDAPAttribute oldPassAttribute = new LDAPAttribute("userPassword", "{MD5}" + oldBase64);
            boolean oldPasswordMatch = lc.compare(userDN, oldPassAttribute);

            if(oldPasswordMatch) {
                digest.update(newPass.getBytes());
                String newBase64 = new BASE64Encoder().encode(digest.digest());
                LDAPModification[] modifications = new LDAPModification[1];
                LDAPAttribute newPassAttribute = new LDAPAttribute("userPassword", "{MD5}" + newBase64);
                modifications[0] = new LDAPModification(LDAPModification.REPLACE, newPassAttribute);
                lc.modify(userDN, modifications);

                modified = true;
            } else {
                throw new Exception("current password is incorrect. please try it again.");
            }


            // disconnect with the server
            lc.disconnect();
        } catch(Exception ex) {
            throw ex;
        }

        return modified;
    }

    public boolean createNewUser(String userName, String firstName, String lastName, String password, String email , String phone, String desc) throws Exception {
        boolean created = false;
        LDAPConnection lc = new LDAPConnection();

        try {
            lc.connect(this.LDAP_HOST, LDAPConnection.DEFAULT_PORT);
            lc.bind(LDAPConnection.LDAP_V3, this.LDAP_ADMIN, this.LDAP_ADMIN_PASS.getBytes("UTF8"));

            String userDN = String.format(this.LDAP_USER_DN, userName);

            LDAPAttributeSet attributeSet=new LDAPAttributeSet();

            LDAPAttribute oc = new LDAPAttribute("objectClass");
            oc.addValue("top");
            oc.addValue("posixAccount");
            oc.addValue("inetOrgPerson");
            attributeSet.add(oc);

            attributeSet.add(new LDAPAttribute("cn", firstName + " " + lastName));
            attributeSet.add(new LDAPAttribute("sn", lastName));
            attributeSet.add(new LDAPAttribute("givenName", firstName));
            attributeSet.add(new LDAPAttribute("homeDirectory", "/home/users/" + userName));
            //attributeSet.add(new LDAPAttribute("email", email));
            attributeSet.add(new LDAPAttribute("uidNumber", "" + CommonTool.getGuid()));
            attributeSet.add(new LDAPAttribute("gidNumber", "502")); // pstuser group

            /*attributeSet.add(new LDAPAttribute("telephone", phone));
            attributeSet.add(new LDAPAttribute("email", email));
            attributeSet.add(new LDAPAttribute("description", desc));*/

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            String newBase64 = new BASE64Encoder().encode(digest.digest());
            LDAPAttribute newPassAttribute = new LDAPAttribute("userPassword", "{MD5}" + newBase64);
            attributeSet.add(newPassAttribute);

            LDAPEntry newEntry=new LDAPEntry(userDN, attributeSet);
            lc.add(newEntry);

            created = true;

            // disconnect with the server
            lc.disconnect();
        } catch(Exception ex) {
            throw ex;
        }

        return created;
    }
}
