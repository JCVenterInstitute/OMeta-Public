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

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 3/2/11
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public final static String SERVICE_NAME = "ometa";
    public static String SCRATCH_BASE_LOCATION = "."+SERVICE_NAME;

    public final static String CONFIGURATION_PREFIX = "ometa";

    public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static String NEW_WINDOW_LINK_HTML = "window.open('%s');";
    public static String A_TAG_HTML = "<a href=%s onclick=%s>";
    public static String A_TAG_CLOSING_HTML = "</a>";

    public static String TAXON_URL= "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=";
    public static String TAXON_URL_PARAM= "&lvl=3&p=mapview&p=has_linkout&p=blast_url&p=genome_blast&lin=f&keep=1&srchmode=1&unlock";
    public static String TRACESRA_URL= "http://www.ncbi.nlm.nih.gov/sites/entrez?db=sra&cmd=Search&dopt=DocSum&term=txid";
    public static String WGS_URL= "http://www.ncbi.nlm.nih.gov/nucleotide/";
    public static String ANNOTATION_URL= "http://www.ncbi.nlm.nih.gov/sites/entrez?db=nucleotide&cmd=Search&term=";
    public static String SAMPLE_DETAIL_URL = "/"+SERVICE_NAME+"/sampleDetail.action?";
    public static String dbSNP_URL = "#";
    public static String PROJECT_SPECIFIC_PAGE = "http://%s.jcvi.org/projects/%s/%s/index.php";
    public static String NCBI_PROJECT_PAGE = "http://www.ncbi.nlm.nih.gov/bioproject/";
    public static String FGSC_URL="http://www.fgsc.net/";
    public static String NARSA_URL="http://www.narsa.net/control/member/repositories";
    public static String BEI_URL="http://www.beiresources.org/";
    public static String STEC_URL="http://www.shigatox.net/new/";
    public static String NCPF_URL="http://www.hpacultures.org.uk/collections/ncpf.jsp";

    public static final String CONIFG_FILE_STORAGE_PATH = "ometa.fileStorage.path";
    public static final String CONFIG_TAREXCLUDE_PATH = "ometa.tarexclude.files.path";

    public static final String PROPERTIES_FILE_NAME = "resource/LoadingEngine";
    public static final String PRODUCTION_DATABASE = "production";
    public static final String DEVELOPMENT_DATABASE = "development";
    public static final String FILE_DOWNLOAD_MSG = "FILE_DOWNLOAD";
    public static final String FILE_FAILURE_MSG = "FILE_NOT_FOUND";
    public static final String FAILURE_MSG = "FAILURE";
    public static final String PROMPT_IN_FILE_PREFIX = "#";

    public static final String DENIED_USER_EDIT_MESSAGE = "You do not have permission to edit the project.";
    public static final String DENIED_USER_VIEW_MESSAGE = "You do not have permission to access the project.";
    public static final String LOGIN_REQUIRED_MESSAGE = "User must first login before attempting to use the requested resources.";
    public static final String INVALID_DATE_MESSAGE = "Your data input is invalid. Please use " + DEFAULT_DATE_FORMAT + " format.";
    public static final String FORBIDDEN_ACTION_RESPONSE = "forbidden";

    public static final String EVENT_PROJECT_REGISTRATION = "ProjectRegistration";
    public static final String EVENT_SAMPLE_REGISTRATION = "SampleRegistration";

    public static final String ATTR_PROJECT_NAME = "Project Name";
    public static final String ATTR_SAMPLE_NAME = "Sample Name";
    public static final String ATTR_PARENT_SAMPLE_NAME = "Parent Sample";
}
