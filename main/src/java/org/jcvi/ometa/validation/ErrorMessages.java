package org.jcvi.ometa.validation;

import org.jcvi.ometa.utils.Constants;

/**
 * User: movence
 * Date: 9/25/14
 * Time: 10:42 AM
 * org.jcvi.ometa.validation
 */
public class ErrorMessages {
    public static final String DENIED_USER_EDIT_MESSAGE = "You do not have permission to edit the project.";
    public static final String DENIED_USER_VIEW_MESSAGE = "You do not have permission to access the project.";
    public static final String LOGIN_REQUIRED_MESSAGE = "User must first login before attempting to use the requested resources.";
    public static final String INVALID_DATE_MESSAGE = "Your data input is invalid. Please use " + Constants.DATE_DEFAULT_FORMAT + " format.";
    public static final String FORBIDDEN_ACTION_RESPONSE = "forbidden";
}
