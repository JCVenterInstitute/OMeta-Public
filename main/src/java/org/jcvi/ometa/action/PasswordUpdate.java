package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.action.ajax.IAjaxAction;
import org.jcvi.ometa.helper.LDAPHelper;

/**
 * User: movence
 * Date: 12/8/14
 * Time: 10:20 PM
 * org.jcvi.ometa.action
 */
public class PasswordUpdate extends ActionSupport implements IAjaxAction, Preparable {
    private Logger logger = Logger.getLogger(PasswordUpdate.class);

    String newPass;
    String newPassRe;
    String oldPass;

    public PasswordUpdate() {
    }

    public String execute() {
        String rtnVal = INPUT;

        try {
            if(oldPass != null && !oldPass.isEmpty()
                    && newPass != null && !newPass.isEmpty()
                    && newPassRe != null && !newPassRe.isEmpty()) {

                if(newPass.equals(newPassRe)) {
                    String userName = ServletActionContext.getRequest().getRemoteUser();

                    LDAPHelper ldapHelper = new LDAPHelper();
                    ldapHelper.updatePassword(userName, oldPass, newPass);
                    addActionMessage("password has been updated successfully.");
                    rtnVal = SUCCESS;
                } else {
                    throw new Exception("password confirmation does not match.");
                }

            }
        } catch(Exception ex) {
            addActionError(ex.getCause() == null ? ex.getMessage() : ex.getCause().toString());
            rtnVal = ERROR;
        }

        return rtnVal;
    }

    @Override
    public String runAjax() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void prepare() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public String getNewPassRe() {
        return newPassRe;
    }

    public void setNewPassRe(String newPassRe) {
        this.newPassRe = newPassRe;
    }

    public String getOldPass() {
        return oldPass;
    }

    public void setOldPass(String oldPass) {
        this.oldPass = oldPass;
    }
}
