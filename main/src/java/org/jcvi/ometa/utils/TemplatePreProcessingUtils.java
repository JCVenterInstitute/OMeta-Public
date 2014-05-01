package org.jcvi.ometa.utils;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.*;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.validation.ModelValidator;
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

        boolean isProjectRegistration = eventName.contains(Constants.EVENT_PROJECT_REGISTRATION);
        //boolean isProjectUpdate = eventName.replaceAll("\\s","").equals("ProjectUpdate");
        boolean isSampleRegistration = eventName.contains(Constants.EVENT_SAMPLE_REGISTRATION);

        List<HeaderDetail> headers = new ArrayList<HeaderDetail>();

        headers.add(new HeaderDetail("ProjectName", true, "string", "", null));

        if (isSampleRegistration) { // parent sample name for sample registration
            headers.add(new HeaderDetail("ParentSample", false, "string", "", null));
        }

        if (isProjectRegistration || isSampleRegistration) { //public flag
            headers.add(new HeaderDetail("Public", true, "int", "", null));
        }

        boolean sampleRequired = false;
        for (EventMetaAttribute ema : emas) {
            sampleRequired |= ema.isSampleRequired();
            headers.add(new HeaderDetail(
                    ema.getLookupValue().getName(), ema.isRequired(),
                    ema.getLookupValue().getDataType(), ema.getOptions(),
                    ema.getLabel()
            ));
        }

        if(isSampleRegistration || sampleRequired) {
            headers.add(1, new HeaderDetail("SampleName", true, "string", "", null));
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
            if(i > 0) {
                csvContents.append(",");
                comments.append(",");
            }
            String attributeHeader = detail.getName();
            if(detail.getLabel() != null) {
                attributeHeader = detail.getLabel() + "[" + attributeHeader + "]";
            }
            csvContents.append(attributeHeader);
            comments.append(this.getComment(detail) + (detail.hasOptions()?detail.getOptionsString():""));
            i++;
        }

        csvContents.append("\n" + comments.toString());
        csvContents.append("\n" +
                (!isProjectRegistration ? projectName : "") +
                (sampleName != null && !sampleName.trim().isEmpty() ? "," + sampleName : "")
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
            cell.setCellValue(this.getComment(detail));
            cell.setCellStyle(redCS);

            if(detail.getDataType().equals(ModelValidator.DATE_DATA_TYPE)) {
                DataFormat df = wb.createDataFormat();
                CellStyle dateCS = wb.createCellStyle();
                CreationHelper createHelper = wb.getCreationHelper();
                dateCS.setDataFormat(createHelper.createDataFormat().getFormat(Constants.DEFAULT_DATE_FORMAT));

                sheet.setDefaultColumnStyle(headerIndex-1, dateCS);
            }

            this.addValidations(headerIndex - 1, detail, sheet);
        }

        //adds project name validation
        if(!isProjectRegistration) {
            Row firstRow = sheet.createRow(rowIndex++);
            firstRow.createCell(0).setCellValue(projectName);
            /*CellRangeAddressList addressList = new CellRangeAddressList(2, 100, 0, 0);
            DVConstraint projectNameConstraint = DVConstraint.createExplicitListConstraint(new String[]{projectName});
            DataValidation projectNameValidation = new HSSFDataValidation(addressList, projectNameConstraint);
            projectNameValidation.setSuppressDropDownArrow(true);
            projectNameValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            projectNameValidation.createErrorBox("A template ONLY supports single project!", "Project: " + projectName);
            sheet.addValidationData(projectNameValidation);*/
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        InputStream templateStream = new ByteArrayInputStream(baos.toByteArray());

        return templateStream;
    }

    private String getComment(HeaderDetail detail) {
        return String.format(this.mutatedComment, detail.getDataType(), detail.isRequired()?"*Required":"optional");
    }

    private void addValidations(int headerIndex, HeaderDetail detail, Sheet sheet) {
        CellRangeAddressList addressList = new CellRangeAddressList(2, 100, headerIndex, headerIndex);
        DVConstraint constraint = null;
        DataValidation validation = null;
        //adds select box with option values
        if(detail.hasOptions()) {
            constraint = DVConstraint.createExplicitListConstraint(detail.getOptionsArray());
            validation = new HSSFDataValidation(addressList, constraint);
            validation.setSuppressDropDownArrow(false);
            sheet.addValidationData(validation);
        }

        //data type validation
        if(detail.getDataType().equals(ModelValidator.DATE_DATA_TYPE)) {
            constraint = DVConstraint.createDateConstraint(
                    DVConstraint.OperatorType.GREATER_THAN,
                    "1900-01-01", "0000-00-00",
                    Constants.DEFAULT_DATE_FORMAT
            );
            validation = new HSSFDataValidation(addressList, constraint);
            validation.setSuppressDropDownArrow(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("Use a valid date format!", Constants.DEFAULT_DATE_FORMAT);
            sheet.addValidationData(validation);
        } else if(detail.getDataType().equals(ModelValidator.INT_DATA_TYPE)) {
            constraint = DVConstraint.createNumericConstraint(
                    DVConstraint.ValidationType.INTEGER,
                    DVConstraint.OperatorType.GREATER_OR_EQUAL,
                    "0", "0"
            );
            validation = new HSSFDataValidation(addressList, constraint);
            validation.setSuppressDropDownArrow(true);
            sheet.addValidationData(validation);
        } else if(detail.getDataType().equals(ModelValidator.FLOAT_DATA_TYPE)) {
            constraint = DVConstraint.createNumericConstraint(
                    DVConstraint.ValidationType.DECIMAL,
                    DVConstraint.OperatorType.GREATER_OR_EQUAL,
                    "0", "0"
            );
            validation = new HSSFDataValidation(addressList, constraint);
            validation.setSuppressDropDownArrow(true);
            sheet.addValidationData(validation);
        }
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
        private String label;

        public HeaderDetail(String name, boolean required, String dataType, String options, String label) {
            this.name = name;
            this.required = required;
            this.dataType = dataType;
            this.options = options;
            this.label = label;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public boolean hasOptions() {
            return this.getOptions() != null && this.getOptions().length() > 0;
        }
        public String getOptions() { return options; }
        public String getOptionsString() {
            return (this.hasOptions() ? ("[" + this.getOptions() + "]") : "");
        }
        public String[] getOptionsArray() {
            return (this.hasOptions() ? this.getOptions().split(";") : null);
        }
        public void setOptions(String options) { this.options = options; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
}
