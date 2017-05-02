package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.jcvi.ometa.action.ajax.IAjaxAction;
import org.jcvi.ometa.configuration.AccessLevel;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.JsonProducer;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.property.PropertyHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * Created by mkuscuog on 3/28/2017.
 */
public class JsonManagement extends ActionSupport implements IAjaxAction {
    private Logger logger = Logger.getLogger(JsonManagement.class);
    private ReadBeanPersister readPersister;

    List<Project> projectList;
    List<LookupValue> attributeList;
    FileBasedConfigurationBuilder<PropertiesConfiguration> builder;

    List<String> fileNameList;

    String projectNames;
    String attributes;
    String screenAttributes;
    String sorting;
    String fileName;
    String filePath;
    String domain;

    List<String> errorMessages;

    public JsonManagement() {
        errorMessages = new ArrayList<>(0);
        fileNameList = new ArrayList<>(9);
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister( props );
        String jsonFilePath = props.getProperty(Constants.CONFIG_JSON_FILE_PATH);

        builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                        .configure(new Parameters().properties()
                                .setFileName(jsonFilePath)
                                .setThrowExceptionOnMissing(false)
                                .setListDelimiterHandler(null)
                                .setIncludesAllowed(false));
    }

    public String execute() {
        String returnValue = ERROR;

        try {
            String userName = ServletActionContext.getRequest().getRemoteUser();
            projectList = readPersister.getAuthorizedProjects(userName, AccessLevel.View);
            attributeList = readPersister.getLookupValueByType(ModelValidator.ATTRIBUTE_LV_TYPE_NAME);

            PropertiesConfiguration config = builder.getConfiguration();

            Iterator<String> fileNameKeys = config.getKeys("fileName");
            fileNameKeys.forEachRemaining(fileNameKey -> fileNameList.add(config.getString(fileNameKey)));

            //Load the first json info
            this.setProperties(config, 0);

            returnValue = SUCCESS;
        }  catch ( ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch( LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            addActionError( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch (ConfigurationException ex) {
            logger.error("Exception in Json Management Action : " + ex.toString());
            errorMessages.add("Could not locate JSON properties file!");
            ex.printStackTrace();
        } catch (Exception ex) {
            logger.error("Exception in Json Management Action : " + ex.toString());
            errorMessages.add(ex.toString());
            ex.printStackTrace();
        }

        errorMessages.forEach(error -> addActionError(error));
        return returnValue;
    }

    public String generate() {
        try {
            String userName = ServletActionContext.getRequest().getRemoteUser();

            if(userName == null) return LOGIN;

            JsonProducer jsonProducer = new JsonProducer();
            List<String> errors = jsonProducer.generateJson();

            if(errors.size() > 0) {
                errorMessages.addAll(errors);
            } else {
                addActionMessage("JSON FILES ARE SUCCESFULLY GENERATED");
                return SUCCESS;
            }
        } catch (Exception ex) {
            logger.error("Exception in Json Management Action : " + ex.toString());
            errorMessages.add( "JSON FILES ARE NOT BEING GENERATED" );
        }

        return ERROR;
    }

    public String updateJsonProducer() {
        String returnValue = ERROR;

        try {
            PropertiesConfiguration config = builder.getConfiguration();
            Iterator<String> fileNameKeys = config.getKeys("fileName");

            String[] index = new String[1];
            fileNameKeys.forEachRemaining(fileNameKey -> {
                if(config.getString(fileNameKey).equals(fileName))
                    index[0] = fileNameKey.split("\\.")[1];
            });

            config.setProperty("projectNames." + index[0], projectNames);
            config.setProperty("attributes." + index[0], attributes);
            config.setProperty("sorting." + index[0], sorting);
            config.setProperty("filePath." + index[0], filePath);
            config.setProperty("screenAttributes." + index[0], screenAttributes);
            config.setProperty("domain." + index[0], domain);

            builder.save();

            addActionMessage("Properties for " + fileName + " successfully updated!");
            returnValue = SUCCESS;
        }  catch(Exception ex) {
            logger.error("Exception in Json Management Action : " + ex.toString());
            errorMessages.add("Properties for " + fileName + " couldn't be updated! Please check the log for details!");
            ex.printStackTrace();
        }

        return returnValue;
    }

    @Override
    public String runAjax() {
        String returnValue = ERROR;

        try {
            PropertiesConfiguration config = builder.getConfiguration();

            Iterator<String> fileNameKeys = config.getKeys("fileName");

            fileNameKeys.forEachRemaining(fileNameKey -> {
                if(config.getString(fileNameKey).equals(fileName))
                    this.setProperties(config, Integer.valueOf(fileNameKey.split("\\.")[1]));
            });

            returnValue = SUCCESS;
        }  catch(Exception ex) {
            logger.error("Exception in Json Management Action : " + ex.toString());
            ex.printStackTrace();
        }

        return returnValue;
    }

    private void setProperties(PropertiesConfiguration propertiesConfiguration, int index) {
        projectNames = propertiesConfiguration.getString("projectNames." + index, "");
        attributes = propertiesConfiguration.getString("attributes." + index, "");
        sorting = propertiesConfiguration.getString("sorting." + index, "");
        fileName = propertiesConfiguration.getString("fileName." + index, "");
        filePath = propertiesConfiguration.getString("filePath." + index, "");
        screenAttributes = propertiesConfiguration.getString("screenAttributes." + index, "");
        domain = propertiesConfiguration.getString("domain." + index, "");
    }

    public List<String> getFileNameList() {
        return fileNameList;
    }

    public void setFileNameList(List<String> fileNameList) {
        this.fileNameList = fileNameList;
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public List<LookupValue> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<LookupValue> attributeList) {
        this.attributeList = attributeList;
    }

    public String getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(String projectNames) {
        this.projectNames = projectNames;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getScreenAttributes() {
        return screenAttributes;
    }

    public void setScreenAttributes(String screenAttributes) {
        this.screenAttributes = screenAttributes;
    }

    public String getSorting() {
        return sorting;
    }

    public void setSorting(String sorting) {
        this.sorting = sorting;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
