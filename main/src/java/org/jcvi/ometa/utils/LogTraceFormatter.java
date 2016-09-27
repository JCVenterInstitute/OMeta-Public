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

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 2/28/11
 * Time: 1:58 PM
 *
 * This will take trace output and format it into a single string, suitable for output to
 * a log.  Note that ".trace()" is a nebulously-supported log4j method, and this application
 * will not rely on it.
 */
public class LogTraceFormatter {

    /** Take the exception, dump its trace to a string and return that. */
    public static String formatStackTrace( Throwable ex ) {
        CharArrayWriter caw = new CharArrayWriter();
        PrintWriter pw = new PrintWriter( caw ) ;
        ex.printStackTrace( pw );
        caw.close();

        return caw.toString();
    }
}
