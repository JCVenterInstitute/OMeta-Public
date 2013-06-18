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

import org.jcvi.ometa.model.Event;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jtc.common.util.scratch.ScratchUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 5/23/11
 * Time: 5:15 PM
 *
 * Has utility methods for dealing with specific tweaks made to TSV files, by this application.
 */
public class TsvPreProcessingUtils {

    /**
     * Package all relevant information for a downloadable template TSV, into a
     * string builder.
     *
     * @param emas meta data about event attributes.
     * @param project which project
     * @param sample which sample
     * @param outputBuilder output goes here.
     */
    public void buildFileContent(
            List<EventMetaAttribute> emas,
            String project,
            String sample,
            StringBuilder outputBuilder
    ) {

        List<String> headers = new ArrayList<String>();
        headers.add( Event.PROJECT_NAME_HEADER );
        headers.add( Event.SAMPLE_NAME_HEADER );
        headers.add( Event.ATTRIBUTE_NAME_HEADER );
        headers.add( Event.ATTRIBUTE_VALUE_HEADER );

        // Collect headers.
        for ( int i = 0; i < headers.size(); i++ ) {
            if ( i > 0 ) {
                outputBuilder.append( "\t" );
            }
            outputBuilder.append( headers.get( i ) );
        }
        outputBuilder.append("\n");

        // Collect meta attribute rows.
        for ( int i = 0; i < emas.size(); i++ ) {
            EventMetaAttribute ema = emas.get( i );
            promptMetaAttribute(
                    headers,
                    project,
                    sample,
                    ema.getLookupValue().getName(),
                    ema.getLookupValue().getDataType(),
                    ema.getOptions(),
                    outputBuilder,
                    ema.isRequired() );
        }

        return;
    }

    /**
     * Wish to support special pre-processing of input files for the event loader.  This method will thus
     * create a file with any such changes, based on the original.
     */
    public File preProcessTsvFile(File originalFile) {
        File outputFile = null;
        try {
            // Setup scratch location.
            String userBase = System.getProperty("user.home");
            ScratchUtils.setScratchBaseLocation(userBase + "/" + Constants.SCRATCH_BASE_LOCATION);
            Long timeStamp = new Date().getTime();
            File scratchLoc = ScratchUtils.getScratchLocation(timeStamp, "EventLoader__" + originalFile.getName());

            outputFile = new File( scratchLoc, originalFile.getName() );

            BufferedReader br = new BufferedReader( new FileReader( originalFile ) );
            PrintWriter pw = new PrintWriter( new FileWriter( outputFile ) );

            String inline = null;
            while ( null != ( inline = br.readLine() ) ) {
                // Will output all lines except those having pound-sign prefixes.
                if ( ! inline.startsWith( "#" ) ) {
                    pw.println( inline );
                }
            }

            outputFile.deleteOnExit();
            br.close();
            pw.close();
        } catch ( Exception ex ) {
            throw new IllegalArgumentException( ex );
        }
        return outputFile;
    }

    /** Will get rid of files created as temporaries by this process. */
    public void eliminatePreProcessedFile( File file ) {
        try {
            if ( file.exists() ) {
                file.delete();
                File parentFile = file.getParentFile();
                // If parent exists, definitely should be deleted.  Separate directory
                // created for each temporary file.  But the grandparent should only be deleted
                // if it is just a numeric value.  Numerically-named directories may be
                // included as grandparents.
                if ( parentFile.canWrite() ) {
                    parentFile.delete();

                    parentFile = parentFile.getParentFile();
                    try {
                        Long.parseLong( parentFile.getName() );
                        parentFile.delete();
                    } catch ( NumberFormatException nfe ) {
                        // Do nothing.  Just don't try and delete anything.
                    }
                }
            }
        } catch ( Exception ex ) {
            System.out.println("WARNING: failed to dispose of an intermediate file " + file.getAbsolutePath() );
        }
    }

    /**
     * Helper method to make prompts for writing back files.
     *
     * @param headers list of headers for file.
     * @param project which project
     * @param sample which sample
     * @param attributeName name of the attribute.
     * @param dataType type of data,
     * @param possibleValues values options.
     * @param outputBuilder put data here.
     * @param isRequired required or not.
     */
    private void promptMetaAttribute(
            List<String> headers,
            String project,
            String sample,
            String attributeName,
            String dataType,
            String possibleValues,
            StringBuilder outputBuilder,
            boolean isRequired ) {

        StringBuilder commentBuilder = new StringBuilder();
        StringBuilder inputLineBuilder = new StringBuilder();
        for ( int j = 0; j < headers.size(); j++ ) {
            if ( j > 0 ) {
                inputLineBuilder.append( "\t" );
            }
            String header = headers.get( j );
            if ( header.contains( Event.ATTRIBUTE_NAME_HEADER ) ) {
                inputLineBuilder.append( attributeName );
            }
            else if ( header.contains( Event.ATTRIBUTE_VALUE_HEADER ) ) {
                // Making a separate, different-line comment prompt to tell user how to enter data.
                commentBuilder.append(Constants.PROMPT_IN_FILE_PREFIX)
                              .append(dataType);

                if ( isRequired ) {
                    commentBuilder.append( "  * Required!" );
                }
                else {
                    commentBuilder.append( "  Optional" );
                }

                if ( possibleValues != null   &&  possibleValues.trim().length() > 0 ) {
                    commentBuilder.append( ", one of [")
                                  .append(possibleValues)
                                  .append("]");
                }
                commentBuilder.append("--");

            }
            else if ( header.contains( Event.PROJECT_NAME_HEADER) ) {
                inputLineBuilder.append( project );
            }
            else if ( header.contains( Event.SAMPLE_NAME_HEADER) ) {
                if ( ! sample.startsWith( "--") ) {
                    inputLineBuilder.append( sample );
                }
            }
        }
        outputBuilder.append( commentBuilder );
        outputBuilder.append( "\n" );
        outputBuilder.append( inputLineBuilder );
        outputBuilder.append( "\n" );
    }

}
