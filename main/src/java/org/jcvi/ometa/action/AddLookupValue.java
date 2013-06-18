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
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.model.LookupValue;
import org.jcvi.ometa.utils.UploadActionDelegate;
import org.jcvi.ometa.validation.ModelValidator;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/6/12
 * Time: 12:46 PM
 */
public class AddLookupValue extends ActionSupport{
    private Logger logger = Logger.getLogger(AddLookupValue.class);

    private String lvDataType;
    private String lvName;
    private String lvType;
    private String w;
    private String lerror;
    private String errorMsg;

    private List<String> dataTypes;
    private List<String> types;

    private ProjectSampleEventWritebackBusiness psewt;

    public AddLookupValue() {}

    public String addLookupValueOpen() {
        setLists();
        return SUCCESS;
    }

    public String addLookupValueProcess() {
        String rtnVal = ERROR;
        UserTransaction tx = null;
        try {
            if(lvDataType!=null && lvName!=null && lvType!=null) {
                tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                UploadActionDelegate udelegate = new UploadActionDelegate();
                psewt = udelegate.initializeBusinessObject(logger, psewt);

                LookupValue lv = new LookupValue();
                lv.setName(lvName);
                lv.setType(lvType);
                lv.setDataType(lvDataType);

                List<LookupValue> lvList = new ArrayList<LookupValue>(1);
                lvList.add(lv);
                psewt.loadLookupValues(lvList);
                rtnVal = SUCCESS;
            }

        } catch(Exception ex) {
            errorMsg = ex.toString();
            lerror = "true";
            try {
                if(tx!=null)
                    tx.rollback();
            } catch (SystemException se) {
                addActionError(se.toString());
            }
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

    private void setLists() {
        ModelValidator modelValidator = new ModelValidator();
        dataTypes = new ArrayList<String>();
        dataTypes.addAll(modelValidator.getValidDataTypes());

        types = new ArrayList<String>();
        types.addAll(modelValidator.getValidLookupValueTypes());
    }

    public String getLvDataType() {
        return lvDataType;
    }

    public void setLvDataType(String lvDataType) {
        this.lvDataType = lvDataType;
    }

    public String getLvName() {
        return lvName;
    }

    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    public String getLvType() {
        return lvType;
    }

    public void setLvType(String lvType) {
        this.lvType = lvType;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public String getLerror() {
        return lerror;
    }

    public void setLerror(String lerror) {
        this.lerror = lerror;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<String> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(List<String> dataTypes) {
        this.dataTypes = dataTypes;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
