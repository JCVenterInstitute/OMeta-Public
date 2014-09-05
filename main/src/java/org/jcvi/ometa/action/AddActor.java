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
import org.jcvi.ometa.model.Actor;
import org.jcvi.ometa.utils.UploadActionDelegate;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 9/25/12
 * Time: 1:28 PM
 */
public class AddActor extends ActionSupport {
    private Logger logger = Logger.getLogger(AddActor.class);

    ProjectSampleEventWritebackBusiness psewt;
    Actor actor;

    public String process() {
        String rtnVal = INPUT;
        UserTransaction tx = null;
        try {
            if(actor!=null && actor.getUsername()!=null && actor.getFirstName()!=null && actor.getLastName()!=null) {
                tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
                tx.begin();

                UploadActionDelegate udelegate = new UploadActionDelegate();
                psewt = udelegate.initializeBusinessObject(logger, psewt);

                psewt.loadActor(actor);
                rtnVal = SUCCESS;
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

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }
}
