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

    public static final String PROPERTIES_FILE_NAME = "resource/LoadingEngine";
    public static final String CONIFG_FILE_STORAGE_PATH = "ometa.fileStorage.path";
    public static final String CONFIG_TAREXCLUDE_PATH = "ometa.tarexclude.files.path";
    public static final String CONFIG_GUID_HOST = "ometa.guid.host";
    public static final String CONFIG_GUID_PORT = "ometa.guid.port";
    public static final String CONFIG_DEFAULT_PROJECT_ID = "default.project.id";
    public static final String PRODUCTION_DATABASE = "production";
    public static final String DEVELOPMENT_DATABASE = "development";
    public static final String FILE_DOWNLOAD_MSG = "FILE_DOWNLOAD";

    public static final String GROUP_GENERAL_EDIT = "General-Edit";
    public static final String GROUP_GENERAL_VIEW = "General-View";

    public static final String LOOKUP_VALUE_TYPE_ACCESS_GROUP = "Access Group";
    public static final String LOOKUP_VALUE_TYPE_EDIT_GROUP = "Edit Group";
    public static final String LOOKUP_VALUE_TYPE_ATTRIBUTE = "Attribute";
    public static final String LOOKUP_VALUE_TYPE_EVENT_STATUS = "Event Status";
    public static final String LOOKUP_VALUE_TYPE_EVENT_TYPE = "Event Type";

    public static final String ACCEPTABLE_CHARACTERS = "A-Za-z0-9=.,;:!?@$%#+()\\[\\]/\\-_'\" ";
    public final static String DATE_DEFAULT_FORMAT = "yyyy-MM-dd";
    public final static String DATE_ALTERNATIVE_FORMAT = "dd-MMM-yyyy";
    public final static String[] DATE_ALL_POSSIBLE_FORMATS = {
            "yyyy",
            "MMM-yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            Constants.DATE_DEFAULT_FORMAT,
            Constants.DATE_ALTERNATIVE_FORMAT,
            "yyyy-MM-dd'T'HH:mm:ss"
    };
    public final static String[] DPCC_DATE_ALL_POSSIBLE_FORMATS = {
            "yyyy",
            "MMM-yyyy",
            "DD-MMM-yyyy"
    };

    public static enum  serologyTestResult {P,N,U};

    public final static String[] DATE_NO_VALUES = {"Unknown", "Not applicable", "Not available", "Available upon request"};

    public final static String[] DPCC_DATE_NO_VALUES = {"Unknown"};

    public static final String DATE_DATA_TYPE = "date";
    public static final String STRING_DATA_TYPE = "string";
    public static final String FLOAT_DATA_TYPE = "float";
    public static final String INT_DATA_TYPE = "int";
    public static final String URL_DATA_TYPE = "url";
    public static final String FILE_DATA_TYPE = "file";

    public static final String DENIED_USER_EDIT_MESSAGE = "You do not have permission to edit the project.";
    public static final String DENIED_USER_VIEW_MESSAGE = "You do not have permission to access the project.";
    public static final String LOGIN_REQUIRED_MESSAGE = "User must first login before attempting to use the requested resources.";
    public static final String INVALID_DATE_MESSAGE = "Your data input is invalid. Please use " + DATE_DEFAULT_FORMAT + " format.";
    public static final String FORBIDDEN_ACTION_RESPONSE = "forbidden";

    public static final String EVENT_PROJECT_REGISTRATION = "ProjectRegistration";
    public static final String EVENT_SAMPLE_REGISTRATION = "SampleRegistration";
    public static final String EVENT_PROJECT_UPDATE = "ProjectUpdate";
    public static final String EVENT_SAMPLE_UPDATE = "SampleUpdate";

    public static final String ATTR_PROJECT_NAME = "Project_ID";
    public static final String ATTR_SAMPLE_NAME = "Sample Name";
    public static final String ATTR_PARENT_SAMPLE_NAME = "Parent Sample";
    public static final String ATTR_PROJECT_STATUS = "Project Status";
    public static final String ATTR_SAMPLE_STATUS = "Sample_Status";
    public static final String ATTR_PUBLIC_FLAG = "Public";
    public static final String ATTR_SUBMISSION_ID = "Submission_ID";
    public static final String ATTR_SAMPLE_IDENTIFIER = "Sample_Identifier";
    public static final String[] READ_ONLY_ATTRIBUTES = {
            Constants.ATTR_SUBMISSION_ID,
            Constants.ATTR_SAMPLE_STATUS
    };
    public static final String[] HIDDEN_ATTRIBUTES = {
            Constants.ATTR_SUBMISSION_ID
    };

    public static final String DPCC_STATUS_EDITING = "Editing";
    public static final String DPCC_STATUS_VALIDATED = "Validated";
    public static final String DPCC_STATUS_SUBMITTED = "Data submitted to DPCC";

    public static final String TEMPLATE_COMMENT_INDICATOR = "#";
    public static final String TEMPLATE_EVENT_TYPE_IDENTIFIER = "DataTemplate";

    //variables for creating external links
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


    /* ERROR MESSAGES */
    public static String ERROR_DPCC_NOT_MODIFIABLE = "Sample already has been submitted to DPCC. No changes can be made.";

    //all should be in lower case for comparison
    public enum DURATION_ATTRIBUTES {duration_of_poultry_exposure,duration_of_wild_bird_exposure,duration_of_swine_exposure
        ,duration_of_human_exposure;

        public static boolean contains(String s) {
            try {
                DURATION_ATTRIBUTES.valueOf(s);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
