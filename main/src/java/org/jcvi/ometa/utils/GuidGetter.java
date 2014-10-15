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

import org.jtc.common.util.guid.GuidBlock;
import org.jtc.common.util.property.PropertyHelper;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 2/16/11
 * Time: 10:23 AM
 *
 * This is a delegate, that is usable/passable and can hide the details of getting a unique ID.
 */
public class GuidGetter {
    private static final int NUM_RETRIES = 5;
    private static final int BETWEEN_RETRIES = 2000;
    public static final String DEFAULT_GUID_HTTP_PREFIX = "http://%s:%s/guid/GuidClientServer?Request=GET&Size=";
    public static final String DEFAULT_GUID_NAMESPACE = "GUID_SERVLET";

    private GuidBlock guidBlock;

    public GuidGetter() {
        guidBlock = new GuidBlock();
    }

    /**
     * Obtain a unique Identifier to back fill here.
     */
    public Long getGuid() throws Exception {
        Long retVal = null;
        Exception latestException = null;

        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        String guidHostName = props.getProperty(Constants.CONFIG_GUID_HOST);
        String guidPort = props.getProperty(Constants.CONFIG_GUID_PORT);

        for ( int i = 0; i < NUM_RETRIES; i++ ) {
            try {
                if (guidBlock == null) {
                    guidBlock = new GuidBlock();
                }

                String hostName = (guidHostName == null ? java.net.InetAddress.getLocalHost().getHostName() : guidHostName);
                String port = (guidPort == null ? "8380" : guidPort);
                retVal = guidBlock.getGuidBlock(DEFAULT_GUID_NAMESPACE, String.format(DEFAULT_GUID_HTTP_PREFIX, hostName, port), 1);
            } catch ( Exception ex ) {
                latestException = ex;
                Thread.sleep( BETWEEN_RETRIES );
            }

        }

        if ( retVal == null ) {
            throw latestException;

        }

        return retVal;
    }

}
