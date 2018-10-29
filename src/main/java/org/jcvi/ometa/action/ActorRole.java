package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import org.apache.log4j.Logger;
import org.jcvi.ometa.action.ajax.IAjaxAction;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.helper.LDAPHelper;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.ActorGroup;
import org.jcvi.ometa.model.Group;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jtc.common.util.property.PropertyHelper;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.*;

/**
 * User: movence
 * Date: 4/11/14
 * Time: 3:38 PM
 * org.jcvi.ometa.action
 */
public class ActorRole extends ActionSupport implements IAjaxAction, Preparable {
    private Logger logger = Logger.getLogger(ActorRole.class);

    private List<Actor> actors;
    private List<Group> groups;

    private Long actorId;
    private List<String> groupIds;

    private List<ActorGroup> actorGroups;
    private List<String> roleTypes;
    private String type;

    private String errorMsg;

    private ReadBeanPersister readPersister;
    ProjectSampleEventWritebackBusiness psewt;

    public ActorRole() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public ActorRole(ReadBeanPersister readPersister) {
        this.readPersister = readPersister;
    }

    @Override
    public void prepare() throws Exception {
        this.actors = this.readPersister.getAllActor();
        this.groups = this.readPersister.getAllGroup();
    }

    public String execute() {
        String rtnVal = INPUT;
        UserTransaction tx = null;
        try {

            if(this.actorId != null && this.groupIds != null) {
                if(type != null && type.equals("reset")) {
                    Actor currentActor = this.readPersister.getActor(this.actorId);
                    if(currentActor != null) {
                        LDAPHelper ldapHelper = new LDAPHelper();
                        ldapHelper.resetPassword(
                                currentActor.getUsername(),
                                currentActor.getFirstName() + " " + currentActor.getLastName(),
                                currentActor.getEmail()
                        );
                    }

                    addActionMessage("user password has been reset.");
                } else {
                    tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                    tx.begin();

                    UploadActionDelegate udelegate = new UploadActionDelegate();
                    psewt = udelegate.initializeBusinessObject(logger, psewt);

                    List<ActorGroup> currentGroups = readPersister.getActorGroup(this.actorId);

                    List<Long> currentGroupsList = new ArrayList<Long>(currentGroups.size());
                    List<ActorGroup> removedActorGroups = new ArrayList<ActorGroup>();

                    for(ActorGroup actorGroup : currentGroups) {
                        if(!groupIds.contains(Long.toString(actorGroup.getGroupId()))) { //actor groups to be removed
                            removedActorGroups.add(actorGroup);
                        } else {
                            currentGroupsList.add(actorGroup.getGroupId());
                        }
                    }

                    List<Group> availableGroups = readPersister.getAllGroup();
                    Map<Long, Group> availableGroupsMap = new HashMap<Long, Group>(availableGroups.size());
                    for(Group group : availableGroups) {
                        availableGroupsMap.put(group.getGroupId(), group);
                    }

                    List<ActorGroup> newActorGroups = new ArrayList<ActorGroup>();
                    for(String id : groupIds) {
                        Long groupId = Long.parseLong(id);
                        if(!currentGroupsList.contains(groupId)) {
                            ActorGroup actorGroup = new ActorGroup();
                            actorGroup.setActorId(actorId);
                            actorGroup.setGroup(availableGroupsMap.get(groupId));
                            actorGroup.setGroupId(groupId);
                            newActorGroups.add(actorGroup);
                        }
                    }

                    if(removedActorGroups.size() > 0) {
                        psewt.deleteActorGroup(removedActorGroups);
                    }
                    if(newActorGroups.size() > 0) {
                        psewt.loadActorGroup(newActorGroups);
                    }

                    rtnVal = SUCCESS;
                    addActionMessage("Actor Roles have been updated.");
                }
            }

        } catch (Exception ex) {
            rtnVal = ERROR;
            try {
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                ex = se;
            }
            addActionError(ex.getMessage());
        } finally {
            try {
                if(tx !=null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
                    tx.commit();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return rtnVal;
    }

    @Override
    public String runAjax() {
        String rtnVal = SUCCESS;

        try {
            if(actorId != null && actorId != 0) {
                actorGroups = readPersister.getActorGroup(actorId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorMsg = "Error while getting actor roles";
        }

        return rtnVal;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public List<ActorGroup> getActorGroups() {
        return actorGroups;
    }

    public void setActorGroups(List<ActorGroup> actorGroups) {
        this.actorGroups = actorGroups;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public List<String> getRoleTypes() {
        return roleTypes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
