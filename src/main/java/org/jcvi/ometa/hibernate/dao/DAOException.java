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

package org.jcvi.ometa.hibernate.dao;

/**
 * Created by IntelliJ IDEA.
 * User: Lfoster
 * Date: Aug 9, 2007
 * Time: 1:38:56 PM
 *
 * Exception to be thrown by all DAOs.
 */
public class DAOException extends Exception {
    private String message;
    public DAOException(Exception ex) {
        super(ex);
    }

    public DAOException(Exception ex, String message) {
        super(ex);
        this.message = message;
    }

    public DAOException(String message) {
        this.message = message;
    }

    public String getMessage() {
        if (message != null)
            return message;
        else
            return super.getMessage();
    }
}
