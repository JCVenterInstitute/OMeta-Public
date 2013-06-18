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

package org.jcvi.ometa;

import org.apache.log4j.Logger;
import org.jcvi.ometa.bean_interface.ProjectSampleEventPresentationBusiness;
import org.jcvi.ometa.bean_interface.ProjectSampleEventWritebackBusiness;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.db_interface.WritebackBeanPersister;
import org.jcvi.ometa.hibernate.dao.StandaloneSessionAndTransactionManager;
import org.jcvi.ometa.utils.*;
import org.jtc.common.util.property.PropertyHelper;
import org.junit.Assert;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/28/12
 * Time: 11:23 AM
 */
public abstract class PSTTestAbstract {
    public final String EDIT_GROUP_NAME = "General-Edit";
    public final String VIEW_GROUP_NAME = "General-Veiw";
    protected LoginDialog userPassProvider;

    public PSTTestAbstract() {}

    public void getUserCrendential() {
        userPassProvider = new LoginDialog();
        userPassProvider.promptForLoginPassword( "Login for Test" );

        if ( userPassProvider.getUsername() == null  ||  userPassProvider.getPassword() == null ) {
            Assert.fail("ERROR: You must enter a username and a password.");
        }
    }

    public StandaloneSessionAndTransactionManager getSessionManager(String dbType) {
        Properties props = PropertyHelper.getHostnameProperties( Constants.PROPERTIES_FILE_NAME );
        return new StandaloneSessionAndTransactionManager( props, dbType.equals("dev")?Constants.DEVELOPMENT_DATABASE:Constants.PRODUCTION_DATABASE );
    }

    public ReadBeanPersister getReadBean(String dbType) {
        return new ReadBeanPersister( this.getSessionManager(dbType) );
    }

    public WritebackBeanPersister getWriteBean(String dbType) {
        return new WritebackBeanPersister(null, this.getSessionManager(dbType));
    }

    public ProjectSampleEventWritebackBusiness getWriteEjb(String type) {
        this.getUserCrendential();
        ProjectSampleEventWritebackBusiness pseb = (new EjbBuilder()).getEjb(
                UploadActionDelegate.EJB_NAME,
                (type.equals("prod")?"jnp://localhost:1399":"jnp://localhost:1299"),
                userPassProvider.getUsername(),
                userPassProvider.getPassword(),
                Logger.getLogger(this.getClass().getName())
        );

        return pseb;
    }

    public ProjectSampleEventPresentationBusiness getReadEjb(String type) {
        this.getUserCrendential();
        ProjectSampleEventPresentationBusiness pseb = (new EjbBuilder()).getEjb(
                PresentationActionDelegate.EJB_NAME,
                (type.equals("prod")?"jnp://localhost:1399":"jnp://localhost:1299"),
                userPassProvider.getUsername(),
                userPassProvider.getPassword(),
                Logger.getLogger(this.getClass().getName())
        );

        return pseb;
    }
}
