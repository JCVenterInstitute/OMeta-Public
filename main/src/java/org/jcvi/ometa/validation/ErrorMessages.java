package org.jcvi.ometa.validation;

import org.jcvi.ometa.utils.Constants;

/**
 * User: movence
 * Date: 9/25/14
 * Time: 10:42 AM
 * org.jcvi.ometa.validation
 */
public class ErrorMessages {
    public static final String DENIED_USER_EDIT_MESSAGE = "You do not have permission to edit the project, or the project does not exist.";
    public static final String DENIED_USER_VIEW_MESSAGE = "You do not have permission to access the project, or the project does not exist.";
    public static final String LOGIN_REQUIRED_MESSAGE = "User must first login before attempting to use the requested resources.";
    public static final String INVALID_DATE_MESSAGE = "Your data input is invalid. Please use " + Constants.DATE_DEFAULT_FORMAT + " format.";
    public static final String FORBIDDEN_ACTION_RESPONSE = "forbidden";

    public static final String PROJECT_NOT_FOUND = "project '%s' does not exist";
    public static final String SAMPLE_NOT_FOUND = "sample does not exist or sample name is empty.";
    public static final String ATTRIBUTE_NOT_FOUND = "attribute '%s' not found.";
    public static final String PROJECT_OR_EVENT_NOT_SELECTED = "Project or Event type is not selected.";
    public static final String ATTRIBUTE_VALUE_MISSING = "attribute value for \"%s\" is missing.";

    public static final String LDAP_USER_CREATE_FAILED = "creating new user in ldap has failed.";

    public static final String EVENT_LOADER_FILE_READ = "error in reading the file.";
    public static final String EVENT_LOADER_MISSING_PROJECT = "project name is missing.";
    public static final String EVENT_LOADER_MISSING_SAMPLE = "sample name is missing.";

    public static final String TEMPLATE_MISSING_HEADER_EVENT = "missing event name in the first row.";
    public static final String TEMPLATE_OVERSIZE = "please use the Bulk Submission to load a large set of data.";
    public static final String TEMPLATE_COLUMN_COUNT_MISMATCH = "number of columns of a data row does not match the number of header columns. check for missing or extra commas.";
    public static final String TEMPLATE_MULTIPLE_PROJECT = "multiple projects are found in the file";
    public static final String TEMPLATE_PROJECT_MISSING = "The '" + Constants.ATTR_PROJECT_NAME + "' field is required and must contain a " + Constants.ATTR_PROJECT_NAME + " registered with the OMETA.";

    public static final String CLI_BATCH_INPUT_FILE_MISSING = "input file does not exist.";
    public static final String CLI_BATCH_CSV_ONLY = "only csv files are supported.";


    public static final String BULK_EVENT_TYPE_MISSING = "event type does not exist in the data file.";
    public static final String BULK_EVENT_TYPE_FORMAT = Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER + " must be '" + Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER + ":<eventName>'";
    public static final String BULK_CSV_FILE_MISSING = "CSV template file is missing in \"%s\".";
    public static final String BULK_MULTIPLE_CSV_FILE = "Each zip file must have only one CSV data template file in it.";

    public static final String SEQUENCE_SAMPLE_IDENTIFIER_MISSING = "'" + Constants.ATTR_SAMPLE_IDENTIFIER + "' value is missing.";
    public static final String SEQUENCE_FILE_MISSING = "sequence file does not exist for \"%s\"";
    public static final String SEQUENCE_MULTILEVEL_ZIP = "sequence zip file has more than one sub-directories.";
    public static final String SEQUENCE_DUPLICATED_ATTRIBUTE = "found duplicated '%s' (%s) in %s";
    public static final String SEQUENCE_MISSING_METADATA = "sequence mapping data is missing.";
    public static final String SEQUENCE_NUMBER_MISMATCH = "the number of samples does not match the number of sequences.";
    public static final String SEQUENCE_SINGLE_SEQUENCE_ONLY = "there are none or multiple " + Constants.ATTR_SAMPLE_IDENTIFIER + " values in the sequence file.";
    public static final String SEQUENCE_SI_NOT_MATCHING = Constants.ATTR_SAMPLE_IDENTIFIER + " value does not match in the sequence file.";
    public static final String SEQUENCE_EXTRA_SEQUENCE_DATA = "Inconsistency between files: Additional sequences found not referenced in sequence metadata file.";
    public static final String SEQUENCE_MISSING_SEGMENT = "sequence meta data is missing a segment value.";

    public static final String ATTRIBUTE_DUPLICATE_ORDER = "Error while processing meta attribute positions. Check for any duplicated position values.";
    public static final String TRANSACTION_ERROR = "Transaction Error! Use Help menu or contact the administrator.";
    public static final String VALIDATION_CLASS_NOT_FOUND = "Error while processing meta attribute. Validation class: %s not found!";
    public static final String VALIDATION_METHOD_NOT_FOUND = "Error while processing meta attribute. Validation method: %s not found!";
    public static final String VALIDATION_CLASS_METHOD_NOT_FOUND = "Error while processing meta attribute. Validation class/method: %s.%s not found!";

}
