package org.jcvi.ometa.utils;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
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
public class TemplatePreProcessingUtils {
    private final String comma = ",";
    private final String required = "*Required";
    private final String optional = "Optional";
    private final String mutatedComment = Constants.PROMPT_IN_FILE_PREFIX.concat("%s %s");

    public InputStream buildFileContent(
            String type, List<EventMetaAttribute> emas,
            String projectName, String sampleName, String eventName) throws Exception {

        boolean isProjectRegistration = eventName.equals(Constants.EVENT_PROJECT_REGISTRATION);
        boolean isProjectUpdate = eventName.replaceAll("\\s","").equals("ProjectUpdate");
        boolean isSampleRegistration = eventName.equals(Constants.EVENT_SAMPLE_REGISTRATION);

        List<HeaderDetail> headers = new ArrayList<HeaderDetail>();

        headers.add(new HeaderDetail("ProjectName", true, "string", ""));

        if (isSampleRegistration) { // parent sample name for sample registration
            headers.add(new HeaderDetail("ParentSample", false, "string", ""));
        }

        if (isProjectRegistration || isSampleRegistration) { //public flag
            headers.add(new HeaderDetail("Public", true, "int", ""));
        }

        boolean sampleRequired = false;
        for (EventMetaAttribute ema : emas) {
            sampleRequired |= ema.isSampleRequired();
            headers.add(new HeaderDetail(
                    ema.getLookupValue().getName(), ema.isRequired(),
                    ema.getLookupValue().getDataType(), ema.getOptions()
            ));
        }

        InputStream templateStream = null;
        if(type.equals("c")) {
            templateStream = this.createCSV(headers, isProjectRegistration, projectName, sampleName);
        } else {
            templateStream = this.createExcel(headers, isProjectRegistration, projectName, sampleName, eventName);
        }

        return templateStream;
    }

    private InputStream createCSV(
            List<HeaderDetail> attributes, boolean isProjectRegistration,
            String projectName, String sampleName) throws Exception {
        StringBuilder csvContents = new StringBuilder();
        StringBuilder comments = new StringBuilder();

        int i = 0;
        for(HeaderDetail detail : attributes) {
            csvContents.append((i > 0 ? "," : "") + "\"" + detail.getName() + "\"");
            comments.append(
                    (i > 0 ? "," : "") +
                            "\"" +
                            String.format(this.mutatedComment, detail.getDataType(), detail.isRequired()?"*Required":"optional") +
                            (detail.getOptions()!=null && detail.getOptions().length()>0 ? ("["+detail.getOptions()+"]") : "") +
                            "\""
            );
            i++;
        }

        csvContents.append("\n" + comments.toString());
        csvContents.append("\n" +
                (!isProjectRegistration ? "\""+projectName+"\"": "") +
                (sampleName != null && !sampleName.trim().isEmpty() ? ",\"" + sampleName + "\"" : "")
        );
        return IOUtils.toInputStream(csvContents.toString());
    }

    private InputStream createExcel(
            List<HeaderDetail> attributes, boolean isProjectRegistration,
            String projectName, String sampleName, String eventName) throws Exception {
        Workbook wb = new HSSFWorkbook();
        //CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet(eventName);

        CellStyle boldCS = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setFontHeightInPoints((short) 12);
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        boldCS.setFont(boldFont);
        CellStyle redCS = wb.createCellStyle();
        Font redFont = wb.createFont();
        redFont.setColor(Font.COLOR_RED);
        redFont.setFontHeightInPoints((short) 12);
        redCS.setFont(redFont);

        int rowIndex = 0;
        Row attributeRow = sheet.createRow(rowIndex++);
        Row commentRow = sheet.createRow(rowIndex++);

        int headerIndex = 0;
        Cell cell = null;
        for(HeaderDetail detail : attributes) {
            cell = attributeRow.createCell(headerIndex);
            cell.setCellValue(detail.getName());
            cell.setCellStyle(boldCS);

            cell = commentRow.createCell(headerIndex++);
            cell.setCellValue(
                    String.format(this.mutatedComment, detail.getDataType(), detail.isRequired()?"*Required":"optional") +
                            (detail.getOptions()!=null && detail.getOptions().length()>0 ? ("["+detail.getOptions()+"]") : "")
            );
            cell.setCellStyle(redCS);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        InputStream templateStream = new ByteArrayInputStream(baos.toByteArray());

        return templateStream;
    }

    public File preProcessTemplateFile(File originalFile) {
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

    private class HeaderDetail {
        private String name;
        private boolean required;
        private String dataType;
        private String options;

        public HeaderDetail(String name, boolean required, String dataType, String options) {
            this.name = name;
            this.required = required;
            this.dataType = dataType;
            this.options = options;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
    }
}
