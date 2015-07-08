package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.exception.ForbiddenResourceException;
import org.jcvi.ometa.exception.LoginRequiredException;
import org.jcvi.ometa.model.Dictionary;
import org.jcvi.ometa.model.DictionaryDependency;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by mkuscuog on 7/7/2015.
 */
public class DictionaryManagement extends ActionSupport {
    private Logger logger = Logger.getLogger(DictionaryManagement.class);

    private ReadBeanPersister readPersister;

    private List<Dictionary> dictionaryList;
    private Map<Long, Long> dependencyMap;
    private String errorMsg;

    public DictionaryManagement() {
        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public String getDictionaries(){
        String returnValue = NONE;

        try {
            this.dictionaryList = readPersister.getDictionaries();
            List<DictionaryDependency> dictionaryDependencyList = readPersister.getDictionaryDependencies();

            dependencyMap = new HashMap<Long, Long>(dictionaryDependencyList.size());

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
            logger.error("Exception in Event Detail Action : " + ex.toString());
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
}
