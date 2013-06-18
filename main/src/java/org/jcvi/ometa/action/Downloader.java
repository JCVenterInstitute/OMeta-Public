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

package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 5/11/11
 * Time: 1:39 PM
 *
 * This throws back a given file.
 */
public class Downloader extends ActionSupport {
    private String fileName;
    private Logger logger = Logger.getLogger(Downloader.class);

    private InputStream fileInputStream;

    public InputStream getFileInputStream() {
        return fileInputStream;
    }

    public String execute() {
        String rtnVal = SUCCESS;

        if ( fileName == null  ||  fileName.trim().length() == 0 ) {
            return Constants.FILE_FAILURE_MSG;
        }
        else {
            File f = new File( fileName );
            if ( f.canRead() ) {
                try {
                    fileInputStream = new FileInputStream( f );
                } catch ( Exception ex ) {
                    logger.error( ex.getMessage() + " for file " + f.getAbsolutePath() );
                    return Constants.FILE_FAILURE_MSG;
                }
            }
            else {
                return Constants.FILE_FAILURE_MSG;
            }
        }
        return rtnVal;
    }

    public void setFilename( String fileName ) {
        this.fileName = fileName;
    }

}
