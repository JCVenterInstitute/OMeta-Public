package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;

import com.opensymphony.xwork2.Preparable;
import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.helper.LDAPHelper;
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.model.ActorGroup;
import org.jcvi.ometa.model.Group;
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
 * Created by mkuscuog on 2/13/2015.
 */
public class EditActor extends ActionSupport implements Preparable {
    private Logger logger = Logger.getLogger(EditActor.class);

    private List<Actor> actors;
    private List<Group> groups;
    private Actor actor;

    private Long actorId;
    private List<String> groupIds;

    private List<ActorGroup> actorGroups;
    private List<String> roleTypes;
    private String type;

    private String errorMsg;

    private ReadBeanPersister readPersister;
    ProjectSampleEventWritebackBusiness psewt;

    public EditActor(){
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public EditActor(ReadBeanPersister readPersister){this.readPersister = readPersister;  }

    @Override
    public void prepare() throws Exception {
        this.actors = this.readPersister.getAllActor();
        this.groups = this.readPersister.getAllGroup();
    }

    public String execute(){
        String rtnVal = NONE;
        UserTransaction tx = null;

        try {
            if(this.actorId != null) {
                if(this.groupIds != null){
                    StringBuffer errors = new StringBuffer();

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
                        this.actor.setLoginId(actorId);
                        throw new Exception(errors.toString());
                    }

                    tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                    tx.begin();

                    UploadActionDelegate udelegate = new UploadActionDelegate();
                    psewt = udelegate.initializeBusinessObject(logger, psewt);

                    //Update Actor
                    Actor currActor = this.readPersister.getActor(this.actorId);
                    currActor.setFirstName(this.actor.getFirstName());
                    currActor.setMiddleName(this.actor.getMiddleName());
                    currActor.setLastName(this.actor.getLastName());
                    currActor.setEmail(this.actor.getEmail());
                    this.psewt.updateActor(currActor);

                    //Update actor groups
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
                    this.setActor(currActor);
                    addActionMessage("Actor Information has been updated.");
                } else {
                    this.actor = this.readPersister.getActor(this.actorId);
                    this.actorGroups = readPersister.getActorGroup(this.actorId);

                    rtnVal = INPUT;
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

    public List<Actor> getActors() {
        return actors;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
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
