package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.model.DictionaryDependency;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.util.*;

/**
 * Created by mkuscuog on 7/7/2015.
 */
public class DictionaryManagement extends ActionSupport {
    private Logger logger = LogManager.getLogger(DictionaryManagement.class);

    private ReadBeanPersister readPersister;

    private List<Dictionary> dictionaryList;
    private Map<Long, Long> dependencyMap;
    private String errorMsg;
    private boolean active;
    private Long dictionaryId;
    private List<String> types;

    private String parentDictType;
    private String parentDictCode;
    private List aaData;

    public DictionaryManagement() {
        readPersister = new ReadBeanPersister();
    }

    public String getDictionaries(){
        String returnValue = NONE;

        try {
            this.dictionaryList = readPersister.getDictionaries(true);
            List<DictionaryDependency> dictionaryDependencyList = readPersister.getDictionaryDependencies();

            dependencyMap = new HashMap<>(dictionaryDependencyList.size());

            for(DictionaryDependency dictDependency : dictionaryDependencyList){
                this.dependencyMap.put(dictDependency.getDictionaryId(), dictDependency.getParentId());
            }
        } catch (ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch(LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch(Exception ex) {
            logger.error("Exception in Dictionary Action : " + ex.toString());
            ex.printStackTrace();
        }

        if(errorMsg != null) addActionError(errorMsg);

        return returnValue;
    }

    public String getDictionariesForAdmin(){
        String returnValue = NONE;

        try {
            this.dictionaryList = readPersister.getDictionaries(true);
            List<DictionaryDependency> dictionaryDependencyList = readPersister.getDictionaryDependencies();

            dependencyMap = new HashMap<>(dictionaryDependencyList.size());

            for(DictionaryDependency dictDependency : dictionaryDependencyList){
                this.dependencyMap.put(dictDependency.getDictionaryId(), dictDependency.getParentId());
            }

            List<Object[]> typeCodePairs = readPersister.getAllDictionaryTypeCodePairs();
            types = new ArrayList<>(typeCodePairs.size());

            for(Object[] pair : typeCodePairs){
                types.add((String) pair[0] + " - " + (String) pair[1]);
            }
        } catch (ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch(LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch(Exception ex) {
            logger.error("Exception in Dictionary Management Action : " + ex.toString());
            ex.printStackTrace();
        }

        if(errorMsg != null) addActionError(errorMsg);

        return returnValue;
    }

    public String updateDictionary() {
        String returnValue = NONE;

        try {
            this.dictionaryList = readPersister.getDictionaries(true);
            readPersister.updateDictionary(dictionaryId, active);

        } catch (ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch(LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch(Exception ex) {
            logger.error("Exception in Update Dictionary Action : " + ex.toString());
            ex.printStackTrace();
        }

        if(errorMsg != null) addActionError(errorMsg);

        return returnValue;
    }

    public String getChildDictionary(){
        String returnValue = NONE;

        try {
            List<Dictionary> childList = readPersister.getDictionaryDependenciesByType(parentDictType, parentDictCode);
            aaData = new ArrayList<String>(childList.size());

            for (Dictionary dictionary : childList) {
                String code = dictionary.getDictionaryCode();
                String value = dictionary.getDictionaryValue();

                aaData.add(code + " - " + value);
            }

            returnValue = SUCCESS;
        } catch (ForbiddenResourceException fre ) {
            logger.info( Constants.DENIED_USER_EDIT_MESSAGE );
            addActionError( Constants.DENIED_USER_EDIT_MESSAGE );
            return Constants.FORBIDDEN_ACTION_RESPONSE;
        } catch(LoginRequiredException lre ) {
            logger.info( Constants.LOGIN_REQUIRED_MESSAGE );
            return LOGIN;
        } catch(Exception ex) {
            logger.error("Exception in Update Dictionary Action : " + ex.toString());
            ex.printStackTrace();
        }

        if(errorMsg != null) addActionError(errorMsg);

        return returnValue;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<Dictionary> getDictionaryList() {
        return dictionaryList;
    }

    public void setDictionaryList(List<Dictionary> dictionaryList) {
        this.dictionaryList = dictionaryList;
    }

    public Map<Long, Long> getDependencyMap() {
        return dependencyMap;
    }

    public void setDependencyMap(Map<Long, Long> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    public Long getDictionaryId() {
        return dictionaryId;
    }

    public void setDictionaryId(Long dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getParentDictType() {
        return parentDictType;
    }

    public void setParentDictType(String parentDictType) {
        this.parentDictType = parentDictType;
    }

    public String getParentDictCode() {
        return parentDictCode;
    }

    public void setParentDictCode(String parentDictCode) {
        this.parentDictCode = parentDictCode;
    }

    public List getAaData() {
        return aaData;
    }

    public void setAaData(List aaData) {
        this.aaData = aaData;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
