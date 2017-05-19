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

package org.jcvi.ometa.utils;

import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackRemote;

import javax.naming.InitialContext;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 5/10/11
 * Time: 4:29 PM
 *
 * Helper class for the upload/template actions.
 */
public class UploadActionDelegate extends EjbBuilder {
    //public static final String EJB_NAME = Constants.SERVICE_NAME + "/OMETA.Writeback/remote"; //"PWS.Writeback";
    public static final String EJB_NAME = Constants.SERVICE_NAME +
            "/ometa_ejb_server//OMETA.Writeback!org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackRemote";
    protected static final String EJB_FAILURE_MSG = "Failed to pickup EJB dependency.";

    /**
     * Fallback method to load up the EJB, if injection fails.
     */
    public ProjectSampleEventWritebackBusiness initializeBusinessObject(
            Logger logger, ProjectSampleEventWritebackBusiness psew) {
        if ( psew == null ) {
            try {
                psew = (ProjectSampleEventWritebackRemote) InitialContext.doLookup("ejb:" + EJB_NAME);
                logger.warn("Had to lookup pse tracker using initial context.");
            } catch ( Exception ex ) {
                logger.error(EJB_FAILURE_MSG);
                ex.printStackTrace();
            }
        }
        return psew;
    }

}
