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

import org.jcvi.ometa.model.EventMetaAttribute;
import org.jtc.common.util.scratch.ScratchUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 5/3/12
 * Time: 11:56 AM
 */
public class CsvPreProcessingUtils {
    private final String comma = ",";
    private final String required = "*Required";
    private final String optional = "Optional";
    private final String mutatedComment = Constants.PROMPT_IN_FILE_PREFIX.concat("%s %s");

    public StringWriter buildFileContent(List<EventMetaAttribute> emas, String projectName, String sampleName, String eventName) {
        StringWriter outputWriter = new StringWriter();
        boolean isProjectRegistration = eventName.equals(Constants.EVENT_PROJECT_REGISTRATION);
        boolean isProjectUpdate = eventName.replaceAll("\\s","").equals("ProjectUpdate");
        boolean isSampleRegistration = eventName.equals(Constants.EVENT_SAMPLE_REGISTRATION);

        List<String> headers = new ArrayList<String>();
        headers.add("ProjectName");
        if (!isProjectRegistration && !isProjectUpdate) {
            headers.add("SampleName");
        }

        List<String> comments = new ArrayList<String>();
        comments.add(String.format(this.mutatedComment, "string", "*Required"));

        if (isSampleRegistration) {
            headers.add("ParentSample");
            comments.add(String.format(this.mutatedComment, "string", "Optional"));
        }
        if (isProjectRegistration || isSampleRegistration) {
            headers.add("Public");
            comments.add(String.format(this.mutatedComment, "int", "*Required"));
        }

        boolean sampleRequired = false;
        for (EventMetaAttribute ema : emas) {
            StringBuilder commentBuilder = new StringBuilder();
            String possibleValues = ema.getOptions();
            sampleRequired |= ema.isSampleRequired();

            if (ema.isRequired()) {
                commentBuilder.append(String.format(this.mutatedComment, ema.getLookupValue().getDataType(), "*Required"));
            } else {
                commentBuilder.append(String.format(this.mutatedComment, ema.getLookupValue().getDataType(), "Optional"));
            }

            if ((possibleValues != null) && (possibleValues.trim().length() > 0)) {
                commentBuilder.append(" [").append(possibleValues).append("]");
            }
            comments.add(commentBuilder.toString());
            headers.add(ema.getLookupValue().getName());
        }

        for (int i = 0; i < headers.size(); i++) {
            outputWriter.write((i > 0 ? "," : "") + "\""  + headers.get(i) + "\"");
        }
        outputWriter.append("\n");

        if (!isProjectRegistration && !isProjectUpdate) {
            String sampleNameComment = "";
            if (sampleRequired || isSampleRegistration) {
                sampleNameComment = String.format(this.mutatedComment, "string", "*Required");
            }
            comments.add(1, sampleNameComment);
        }

        for (int i = 0; i < comments.size(); i++) {
            outputWriter.write((i > 0 ? "," : "") + "\"" + comments.get(i) + "\"");
        }
        outputWriter.write("\n");
        outputWriter.write(
                (!isProjectRegistration ? "\""+projectName+"\"": "") +
                (sampleName != null && !sampleName.trim().isEmpty() ? ",\"" + sampleName + "\"" : "")
        );

        return outputWriter;
    }

    public File preProcessCsvFile(File originalFile) {
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
}
