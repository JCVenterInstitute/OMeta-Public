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

package org.jcvi.ometa.production_etl;

import org.jcvi.ometa.production_etl.dbi.ProductionLoaderConnectionFactory;

import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 3/23/11
 * Time: 2:39 PM
 * <p/>
 * Computes insert statements to repopulate production; deletes old production data; runs
 * the insert statements.
 */
public class ProductionLoader {
    protected static final String NONPUBLIC_INSERT_RESOURCE_PREFIX = "resource_internal/gen_";
    protected static final String PRODUCTION_INSERT_RESOURCE_PREFIX = "resource/gen_";
    protected static final String INSERT_RESOURCE_SUFFIX = "_insert.sql";
    protected static final String TURN_OFF_FK_CONSTRAINTS = "SET foreign_key_checks=0";
    protected static final String TURN_ON_FK_CONSTRAINTS = "SET foreign_key_checks=1";
    private ProductionLoaderConnectionFactory dbInterface;
    private boolean pushNonPublicData = false;

    private static final String SCHEMA_PREFIX = "ifx_projects";
    private static final String DELETION_DDL_FMT = "delete from {0}";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("USAGE: java " + ProductionLoader.class.getName() +
                    " <environment> [1=Push Non Public Data, 0 or omitted=Only Public Data]");
            System.out.println("       Where environment is 'test' (for internal) or 'prod' (transfer to external).");
        } else {
            String dbEnvironment = args[0];

            ProductionLoaderConnectionFactory connectionFactory = new ProductionLoaderConnectionFactory(dbEnvironment);
            ProductionLoader productionLoader = new ProductionLoader(connectionFactory);
            if (args.length > 1 && args[1].equals("1")) {
                productionLoader.setPushNonPublicData(true);
            }

            productionLoader.process();
        }
    }

    public ProductionLoader(ProductionLoaderConnectionFactory dbInterface) {
        this.dbInterface = dbInterface;
    }

    public void setPushNonPublicData(boolean pushNonPublicData) {
        this.pushNonPublicData = pushNonPublicData;
    }

    /**
     * Deletes old data in all tables.  Adds new data to affected tables.
     * <p/>
     * Will take the name given, to be a file.  Open that files, execute its "meta SQL" to produce more SQL
     * and run that, too.
     */
    public void process() {
        try {
            // Get one stream per resource, where each resource represents one of the database tables.
            List<InputStream> tableInsertStreams = getTableInsertStreams();

            // NOTE on transactions:  Need exactly one, because multiple tables are involved, which
            // can adversely affect each other.  Also, want to keep the transaction open across all
            // operations, because deletion of the whole database without complete re-build is not
            // acceptable.
            Connection targetConn = dbInterface.getTargetConnection();
            targetConn.setAutoCommit(false);

            // Allow database to ignore referential integrity problems during ETL.
            doUpdate(TURN_OFF_FK_CONSTRAINTS);

            // Delete all data from tables.
            //HKIM: Skip table deletion process so that newly inserted or modified data gets pushed
            //HKIM: Added input check for ETL job which mirrors data from dmz to dev (required parameters "int 1") - 8/27/12
            if(pushNonPublicData && dbInterface.getTargetEnvironment().equals("int")) {
                for (String table : dbInterface.getTableList()) {
                    deleteTable(table);
                }
            }

            // Re-populate all the data.
            insertData(tableInsertStreams);

            // Allow database to catch referential integrity problems during ETL.
            doUpdate(TURN_ON_FK_CONSTRAINTS);

            targetConn.commit();
            dbInterface.closeConnections();

        } catch (Exception ex) {
            //new EmailSender().send( "etl", "[PST} Failure in ETL Process", ex.toString() );
            System.out.println("FAILED to carry out database update.  See error below.");
            ex.printStackTrace();
        }
    }

    /**
     * Go through tables, and make sure that if deleted, their data will be re-created.  In
     * the process, create the list of all the resources that will be used in creating
     * the table data.
     */
    private List<InputStream> getTableInsertStreams() throws Exception {
        List<InputStream> rtnList = new ArrayList<InputStream>();
        for (String table : dbInterface.getTableList()) {
            String resourceName = createResourceName(table);
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                throw new IllegalArgumentException("Failed to find resource " + resourceName + " to insert into table " + table);
            }
            rtnList.add(is);
        }
        return rtnList;
    }

    /**
     * This will kill all data in the table whose name is given.
     *
     * @param tableName what to truncate.
     * @throws Exception thrown by called methods.
     */
    private void deleteTable(String tableName) throws Exception {
        String deletionDDL = MessageFormat.format(DELETION_DDL_FMT, SCHEMA_PREFIX + "." + tableName);
        doUpdate(deletionDDL);
    }

    private void doUpdate(String updateSQL) throws Exception {
        Connection targetConnection = dbInterface.getTargetConnection();
        Statement stmt = targetConnection.createStatement();
        stmt.executeUpdate(updateSQL);
        stmt.close();
    }

    /**
     * Makes and executes input statements which it finds in the input streams given.
     *
     * @param inputStreams list of streams with SQL statements.
     * @throws Exception thrown for called methods.
     */
    private void insertData(List<InputStream> inputStreams) throws Exception {

        Connection targetConn = dbInterface.getTargetConnection();

        for (InputStream nextInputStream : inputStreams) {
            String query = getInsertMakerQuery(nextInputStream);

            // Now, the results of that query represent insert statements, one per row.
            Connection sourceConn = dbInterface.getSourceConnection();
            Statement fetchStmt = sourceConn.createStatement();
            ResultSet fetchSourceResults = getResultSet(query, fetchStmt);

            Statement batchStmt = targetConn.createStatement();
            while (fetchSourceResults.next()) {
                StringBuilder targetInsertBuilder = new StringBuilder();
                ResultSetMetaData metadata = fetchSourceResults.getMetaData();
                for (int i = 0; i < metadata.getColumnCount(); i++) {
                    int colnum = i + 1;

                    Object value = null;
                    //System.out.println("COLNUM=" + colnum + ", TYPE=" + metadata.getColumnType(colnum) + ", TYPENAME=" + metadata.getColumnTypeName(colnum));
                    if (metadata.getColumnTypeName(colnum).equalsIgnoreCase("VARBINARY")) {
                        Blob blob = fetchSourceResults.getBlob(colnum);
                        value = getBlobResult(blob);
                    } else {
                        value = fetchSourceResults.getObject(colnum);
                    }
                    if (value != null) {
                        // Must escape the single and double-quote values, so they do not cause SQL mishaps.
                        // However, there can be escaped single quotes in the string already.  To avoid making
                        // four quotes next to each (or 6, 8, etc.) will first ENSURE there are no escaped ones,
                        // and then escape the single-quotes which then remain.
                        String tempStr = value.toString();
                        boolean reWrap = false;
                        if (tempStr.startsWith("\"") && tempStr.endsWith("\"")) {
                            tempStr = tempStr.substring(1, tempStr.length() - 1);
                            reWrap = true;
                        }

                        tempStr = tempStr.replaceAll("''", "'");
                        tempStr = tempStr.replaceAll("'", "''");
                        // Must change double quotes into backslashed-double-quotes. Hence 1:3 backslashes.
                        tempStr = tempStr.replaceAll("\"", "\\\\\"");
                        if (reWrap) {
                            targetInsertBuilder.append("\"");  // Put old double-qoute back on end.
                        }
                        targetInsertBuilder.append(tempStr);
                        if (reWrap) {
                            targetInsertBuilder.append("\"");  // Put old double-qoute back on end.
                        }
                    } else
                        targetInsertBuilder.append("NULL");

                }

                String targetInsertStatement = targetInsertBuilder.toString();
                batchStmt.addBatch(targetInsertStatement);

                if (fetchSourceResults.getRow() <= 10)
                    System.out.println(targetInsertStatement);

            }
            fetchStmt.close();

            batchStmt.executeBatch();
            batchStmt.close();
        }

    }

    /**
     * Takes all characters out of the blob to package up neatly.
     *
     * @param blob where to get data.
     * @return string based on data contents.
     * @throws Exception thrown for called methods.
     */
    private String getBlobResult(Blob blob) throws Exception {
        String rtnVal = null;
        if (blob != null) {
            Reader rdr = new InputStreamReader(blob.getBinaryStream());
            BufferedReader br = new BufferedReader(rdr);
            StringBuilder bldr = new StringBuilder();
            String inline = null;
            while (null != (inline = br.readLine())) {
                bldr.append(inline);
            }
            br.close();
            rtnVal = bldr.toString();
        }

        return rtnVal;
    }

    /**
     * Gets the results of running the query.
     *
     * @param query     what to run to get answers.
     * @param fetchStmt statement to run query on.
     * @return set of db results.
     * @throws Exception
     */
    private ResultSet getResultSet(String query, Statement fetchStmt) throws Exception {
        ResultSet fetchSourceResults = null;
        try {
            fetchSourceResults = fetchStmt.executeQuery(query);
        } catch (Exception ex) {
            System.out.println("Exception on This SQL: [");
            System.out.println(query);
            System.out.println("]");
            throw ex;
        }
        return fetchSourceResults;
    }

    /**
     * Reads input stream of SQL data and makes a valid query out of it.
     *
     * @param nextInputStream where to get the SQL.
     * @return fully-formatted SQL
     * @throws IOException thrown for called methods.
     */
    private String getInsertMakerQuery(InputStream nextInputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(nextInputStream));

        // All data in the input stream is concatenated into one SQL statement.
        StringBuilder statementMakerBuilder = new StringBuilder();

        String inline = null;
        while (null != (inline = br.readLine())) {
            if (!inline.startsWith("--")) {
                statementMakerBuilder.append(inline).append(' ');
            }
        }
        br.close();
        return statementMakerBuilder.toString();
    }

    protected String createResourceName(String tableName) {
        String resourceName = null;
        if (pushNonPublicData) {
            resourceName = NONPUBLIC_INSERT_RESOURCE_PREFIX + tableName + INSERT_RESOURCE_SUFFIX;
        } else {
            resourceName = PRODUCTION_INSERT_RESOURCE_PREFIX + tableName + INSERT_RESOURCE_SUFFIX;
        }
        return resourceName;
    }

}
