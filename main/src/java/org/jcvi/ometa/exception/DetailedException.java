package org.jcvi.ometa.exception;

/**
 * User: movence
 * Date: 9/15/14
 * Time: 7:47 AM
 * org.jcvi.ometa.stateless_session_bean
 */
public class DetailedException extends Exception {
    int rowIndex;
    boolean isParse;
    boolean isForbidden;

    public DetailedException(String message) throws Exception {
        super(message);
    }

    public DetailedException(int rowIndex, String message) throws Exception {
        this(message);
        this.rowIndex = rowIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public boolean isParse() {
        return isParse;
    }

    public void setParse(boolean parse) {
        isParse = parse;
    }

    public boolean isForbidden() {
        return isForbidden;
    }

    public void setForbidden(boolean forbidden) {
        isForbidden = forbidden;
    }
}
