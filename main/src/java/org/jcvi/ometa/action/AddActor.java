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

package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.helper.LDAPHelper;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.ActorGroup;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ErrorMessages;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 9/25/12
 * Time: 1:28 PM
 */
public class AddActor extends ActionSupport {
    private Logger logger = Logger.getLogger(AddActor.class);

    private ProjectSampleEventWritebackBusiness psewt;
    private ReadBeanPersister readPersister;
    Actor actor;

    private final String[] ACTOR_ADMINS = {
            "ActorAdmin"
    };

    public AddActor() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public String process() {
        String rtnVal = INPUT;
        UserTransaction tx = null;

        try {
            String userName = ServletActionContext.getRequest().getRemoteUser();

            boolean isActorAdmin = false;
            Set<String> dataRoleSet = new HashSet<String>(Arrays.asList(this.ACTOR_ADMINS));
            // check if current user has proper role
            Actor adminActor = this.readPersister.getActorByUserName(userName);
            List<ActorGroup> actorGroups = this.readPersister.getActorGroup(adminActor.getLoginId());
            for(ActorGroup ag : actorGroups) {
                if(dataRoleSet.contains(ag.getGroup().getGroupNameLookupValue().getName())) {
                    isActorAdmin = true;
                    break;
                }
            }

            if(!isActorAdmin) { // actor does not have access privilege to this function
                rtnVal = "denied";
            } else {

                if(this.actor != null && this.actor.getUsername() != null) {
                    StringBuffer errors = new StringBuffer();

                    String userId = this.actor.getUsername();
                    if(userId == null || userId.isEmpty() || userId.length() < 3 || userId.length() > 30) {
                        errors.append("User ID must be at least 3 characters and less than 30 characters long.\n");
                    }
                    if(this.actor.getFirstName() == null || this.actor.getFirstName().isEmpty()) {
                        errors.append("First name is empty.\n");
                    }
                    if(this.actor.getLastName() == null || this.actor.getLastName().isEmpty()) {
                        errors.append("Last name is empty.\n");
                    }
                    String userEmail = this.actor.getEmail();
                    if(userEmail == null || userEmail.isEmpty() || userEmail.length() > 50) {
                        errors.append("User email is empty or too long (max 50 characters).\n");
                    }
                    if(!errors.toString().isEmpty()) {
                        throw new Exception(errors.toString());
                    }

                    LDAPHelper ldapHelper = new LDAPHelper();

                    StringBuffer actorLdapDesc = new StringBuffer();
                    /*actorLdapDesc.append("CEIRS Center Name:").append(this.actor.getCenterName()).append(", ");
                    actorLdapDesc.append("CEIRS Center Role:").append(this.actor.getRole()).append(", ");
                    actorLdapDesc.append("Lab PI Name:").append(this.actor.getPiName()).append(", ");
                    actorLdapDesc.append("Lab PI Email:").append(this.actor.getPiName());*/

                    boolean actorCreated = ldapHelper.createNewUser(
                            this.actor.getUsername(), this.actor.getFirstName(),
                            this.actor.getLastName(), this.actor.getPassword(),
                            this.actor.getEmail(), "6666666666"/*this.actor.getPhone()*/,
                            actorLdapDesc.toString()
                    );

                    if(actorCreated) {
                        tx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
                        tx.begin();

                        UploadActionDelegate udelegate = new UploadActionDelegate();
                        this.psewt = udelegate.initializeBusinessObject(logger, this.psewt);

                        this.psewt.loadActor(actor);
                        rtnVal = SUCCESS;
                    } else {
                        throw new Exception(ErrorMessages.LDAP_USER_CREATE_FAILED);
                    }
                }
            }
        } catch (Exception ex) {
            rtnVal = ERROR;
            try {
                if(tx != null)
                    tx.rollback();
            } catch (SystemException se) {
                ex = se;
            }
            addActionError(ex.getMessage());
        } finally {
            try {
                if(tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                    tx.commit();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return rtnVal;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }
}
