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

package org.jcvi.ometa.production_etl.dbi;

import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 3/24/11
 * Time: 11:20 AM
 * <p/>
 * Carry out JDBC connectivity operations.
 */
public class ProductionLoaderConnectionFactory {
    private static final String MYSQL_DRIVER_CLASSNAME = "com.mysql.jdbc.Driver";
    protected final String CONFIG_LOADER_DB_PREFIX = "etl.target_db_";
    protected final String CONFIG_LOADER_TABLE_LIST = "etl.table_list.";
    protected static final String TEST_DB_ENVIRONMENT = "test";
    protected static final String PRODUCTION_DB_ENVIRONMENT = "prod";

    private Connection _targetConn;
    private Connection _sourceConn;
    private String targetConnectionUrl;
    private String targetConnectionUser;
    private String targetConnectionPassword;

    private String sourceConnectionUrl;
    private String sourceConnectionUser;
    private String sourceConnectionPassword;
    private String[] tableList;

    private String targetEnvironment;

    /**
     * Use this if the props are from the usual place, and all values are properties-bound.
     */
    public ProductionLoaderConnectionFactory(String dbEnvironment) {
        // Get props.
        this(PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME), dbEnvironment);
    }

    /**
     * Use this if all values are properties-bound.  Allows use of alternate properties source.
     */
    public ProductionLoaderConnectionFactory(Properties props, String dbEnvironment) {
        if (!isValidDbEnvironment(dbEnvironment)) {
            System.out.println("WARN: Unexpected db environment " + dbEnvironment + " beware of downstream failures.");
        }
        this.setTargetEnvironment(dbEnvironment);

        String tgtDbUrl = props.getProperty(CONFIG_LOADER_DB_PREFIX+"url." + dbEnvironment);
        String tgtDbUser = props.getProperty(CONFIG_LOADER_DB_PREFIX+"username." + dbEnvironment);
        String tgtDbPass = props.getProperty(CONFIG_LOADER_DB_PREFIX+"password." + dbEnvironment);

        String srcDbUrl = props.getProperty(CONFIG_LOADER_DB_PREFIX+"url." + dbEnvironment);
        String srcDbUser = props.getProperty(CONFIG_LOADER_DB_PREFIX+"username." + dbEnvironment);
        String srcDbPass = props.getProperty(CONFIG_LOADER_DB_PREFIX+"password." + dbEnvironment);
        String tableListStr = props.getProperty(CONFIG_LOADER_TABLE_LIST + dbEnvironment);
        if (tableListStr == null) {
            tableListStr = props.getProperty(CONFIG_LOADER_TABLE_LIST+PRODUCTION_DB_ENVIRONMENT);  // Use production as default.
        }
        tableList = tableListStr.split(",");

        setTargetConnectionUrl(tgtDbUrl);
        setTargetConnectionUser(tgtDbUser);
        setTargetConnectionPassword(tgtDbPass);

        setSourceConnectionUrl(srcDbUrl);
        setSourceConnectionUser(srcDbUser);
        setSourceConnectionPassword(srcDbPass);

    }

    public String[] getTableList() {
        return tableList;
    }

    /**
     * Setters for connection-oriented data.
     */
    public void setTargetConnectionUrl(String url) {
        this.targetConnectionUrl = url;
    }

    public void setTargetConnectionPassword(String password) {
        this.targetConnectionPassword = password;
    }

    public void setTargetConnectionUser(String user) {
        this.targetConnectionUser = user;
    }

    public void setSourceConnectionUrl(String sourceConnectionUrl) {
        this.sourceConnectionUrl = sourceConnectionUrl;
    }

    public void setSourceConnectionUser(String sourceConnectionUser) {
        this.sourceConnectionUser = sourceConnectionUser;
    }

    public void setSourceConnectionPassword(String sourceConnectionPassword) {
        this.sourceConnectionPassword = sourceConnectionPassword;
    }

    public Connection getTargetConnection() throws Exception {
        if (_targetConn == null) {
            Class.forName(MYSQL_DRIVER_CLASSNAME).newInstance();
            _targetConn =
                    DriverManager.getConnection(targetConnectionUrl, targetConnectionUser, targetConnectionPassword);
            _targetConn.setAutoCommit(false);
        }
        return _targetConn;
    }

    public Connection getSourceConnection() throws Exception {
        if (_sourceConn == null) {
            Class.forName(MYSQL_DRIVER_CLASSNAME).newInstance();
            _sourceConn =
                    DriverManager.getConnection(sourceConnectionUrl, sourceConnectionUser, sourceConnectionPassword);
        }
        return _sourceConn;
    }

    /**
     * Convenience: close all connections.
     */
    public void closeConnections() {
        closeConnection(_sourceConn);
        closeConnection(_targetConn);
    }

    private void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception ex) {
            System.out.println("WARN: failed to close connections.");
        }
    }


    /**
     * Check: asking for a sane environment?
     */
    public static boolean isValidDbEnvironment(String dbEnvironment) {
        return dbEnvironment.equalsIgnoreCase(TEST_DB_ENVIRONMENT) || dbEnvironment.equalsIgnoreCase(PRODUCTION_DB_ENVIRONMENT);
    }

    public String getTargetEnvironment() {
        return targetEnvironment;
    }

    public void setTargetEnvironment(String targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }
}
