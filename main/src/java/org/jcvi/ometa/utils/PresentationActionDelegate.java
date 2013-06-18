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
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;

import javax.naming.InitialContext;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 5/10/11
 * Time: 4:29 PM
 *
 * Helper class for the presentation/reporting actions.
 */
public class PresentationActionDelegate extends EjbBuilder {
    public static final String EJB_NAME = Constants.SERVICE_NAME + "/OMETA.Presentation/remote"; //"PWS.Presentation";

    /**
     * Fallback method to load up the EJB, if injection fails.
     */
    public ProjectSampleEventPresentationBusiness initializeEjb(
            Logger logger, ProjectSampleEventPresentationBusiness pseb) {
        if ( pseb == null ) {
            try {
                pseb = InitialContext.doLookup(EJB_NAME);
                logger.warn("Had to lookup pse presentation bean using initial context.");
            } catch ( Exception ex ) {
                logger.error("Failed to pickup EJB dependency.");
                ex.printStackTrace();
            }
        }
        return pseb;
    }

}
