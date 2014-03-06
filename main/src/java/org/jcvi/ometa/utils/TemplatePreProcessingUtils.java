package org.jcvi.ometa.utils;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.*;
import org.jcvi.ometa.model.EventMetaAttribute;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.model.GridBean;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.scratch.ScratchUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 5/3/12
 * Time: 11:56 AM
 */
public class TemplatePreProcessingUtils {
    private final String mutatedComment = Constants.PROMPT_IN_FILE_PREFIX.concat("%s %s %s");

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

        //add sample name attribute right next to project name in a template file
        if(sampleRequired) {
            headers.add(1, new HeaderDetail("Sample", true, "string", ""));
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
            comments.append((i > 0 ? "," : "")
                    + "\""
                    + this.getComment(detail) + (detail.hasOptions()?detail.getOptionsString():"")
                    + "\"");
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
        return String.format(
                this.mutatedComment,
                detail.getDataType(),
                detail.isRequired()?"*Required":"optional",
                detail.getDataType().equals("date") ? "'" + Constants.DEFAULT_DATE_FORMAT + "'" : ""
        );
    }

    private void addValidations(int headerIndex, HeaderDetail detail, Sheet sheet) {
        CellRangeAddressList addressList = new CellRangeAddressList(2, 100, headerIndex, headerIndex);
        DVConstraint constraint = null;
        DataValidation validation = null;
        //adds select box with option values
        if(detail.hasOptions()) {
            //remove "multi(", ")" wrapper for multi-select option values
            String[] optionArray = detail.getOptionsArray();
            if(optionArray.length > 1) {
                String first = optionArray[0];
                if(first.startsWith("multi(")) {
                    optionArray[0] = first.substring(6);
                    String last = optionArray[optionArray.length - 1];
                    optionArray[optionArray.length - 1] = last.substring(0, last.length() - 1);
                }
            }
            constraint = DVConstraint.createExplicitListConstraint(optionArray);
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

    public List<GridBean> parseLoadedFile(
            String originalFileName, File uploadedFile, String projectName,
            boolean isProjectRegistration, boolean isSampleRegistration
    ) throws Exception {
        List<GridBean> gridBeans = new ArrayList<GridBean>();

        String fileType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

        List<String> columns = new ArrayList<String>();
        String currProjectName = null;
        boolean hasSampleName = false;

        //Excel or CSV
        if(fileType.startsWith("xls")) {
            Workbook workbook = WorkbookFactory.create(uploadedFile);
            if(workbook != null && workbook.getNumberOfSheets() > 0) {
                //only cares the first sheet
                Sheet sheet = workbook.getSheetAt(0);
                //check if the sheet has any data by skipping attribute and comment lines
                if(sheet != null && sheet.getLastRowNum() > 1) {
                    //get columns from excel sheet
                    Row attributeNames = sheet.getRow(0);
                    for(int i = 0; i < attributeNames.getLastCellNum(); i++) {
                        columns.add(attributeNames.getCell(i).getStringCellValue());
                    }
                    hasSampleName = columns.indexOf("SampleName") >= 0;

                    int startingRow = 1;
                    Row metaRow = sheet.getRow(startingRow);
                    String firstMetaColumn = metaRow.getCell(0).getStringCellValue();
                    if(!firstMetaColumn.isEmpty() && firstMetaColumn.startsWith("#") && firstMetaColumn.indexOf("string") > 0) {
                        startingRow = 2; //skip the second line that holds metadata of each column
                    }


                    for(int i = startingRow; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        int colIndex = 0;

                        currProjectName = row.getCell(colIndex++).getStringCellValue();
                        if (!isProjectRegistration && !currProjectName.equals(projectName)) {
                            throw new Exception("Multiple projects are found in the file");
                        }

                        GridBean gBean = new GridBean();
                        gBean.setProjectName(currProjectName);

                        if (hasSampleName) {
                            gBean.setSampleName(this.getExcelCellValue(row.getCell(colIndex++)));
                        }

                        if (isProjectRegistration) {
                            gBean.setProjectName(currProjectName);
                            gBean.setProjectPublic(this.getExcelCellValue(row.getCell(colIndex++)));
                        } else if (isSampleRegistration) {
                            gBean.setParentSampleName(this.getExcelCellValue(row.getCell(colIndex++)));
                            gBean.setSamplePublic(this.getExcelCellValue(row.getCell(colIndex++)));
                        }

                        gBean.setBeanList(new ArrayList<FileReadAttributeBean>());
                        for (; colIndex < columns.size(); colIndex++) {
                            FileReadAttributeBean fBean = new FileReadAttributeBean();
                            fBean.setProjectName(isProjectRegistration ? currProjectName : projectName);
                            fBean.setAttributeName(columns.get(colIndex));
                            fBean.setAttributeValue(this.getExcelCellValue(row.getCell(colIndex)));
                            gBean.getBeanList().add(fBean);
                        }
                        gridBeans.add(gBean);
                    }
                }
            }
        } else {
            CSVReader reader = new CSVReader(new FileReader(uploadedFile));

            String[] line;
            int lineCount = 0;

            while ((line = reader.readNext()) != null) {
                if (lineCount == 0) { //headers
                    Collections.addAll(columns, line);
                    hasSampleName = columns.indexOf("SampleName") >= 0;
                } else {
                    int colIndex = 0;

                    if(lineCount == 1) {
                        //skip the second line that holds metadata of each column
                        String firstMetaColumn = line[colIndex];
                        if(!firstMetaColumn.isEmpty() && firstMetaColumn.startsWith("#") && firstMetaColumn.indexOf("string") > 0) {
                            lineCount++;
                            continue;
                        }
                    }

                    currProjectName = line[colIndex++];

                    if(!currProjectName.isEmpty()) {
                        if (!isProjectRegistration && !currProjectName.equals(projectName)) {
                            throw new Exception("Multiple projects are found in the file");
                        }

                        GridBean gBean = new GridBean();
                        gBean.setProjectName(currProjectName);

                        if (hasSampleName) {
                            gBean.setSampleName(line[(colIndex++)]);
                        }

                        if (isProjectRegistration) {
                            gBean.setProjectName(currProjectName);
                            gBean.setProjectPublic(line[(colIndex++)]);
                        } else if (isSampleRegistration) {
                            gBean.setParentSampleName(line[(colIndex++)]);
                            gBean.setSamplePublic(line[(colIndex++)]);
                        }

                        gBean.setBeanList(new ArrayList<FileReadAttributeBean>());
                        for (; colIndex < columns.size(); colIndex++) {
                            FileReadAttributeBean fBean = new FileReadAttributeBean();
                            fBean.setProjectName(isProjectRegistration ? currProjectName : projectName);
                            fBean.setAttributeName(columns.get(colIndex));
                            fBean.setAttributeValue(line[colIndex]);
                            gBean.getBeanList().add(fBean);
                        }
                        gridBeans.add(gBean);
                    }
                }
                lineCount++;
            }
        }

        return gridBeans;
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
        public boolean hasOptions() {
            return this.getOptions()!=null && this.getOptions().length()>0;
        }
        public String getOptions() { return options; }
        public String getOptionsString() {
            return (this.hasOptions()? ("["+this.getOptions()+"]") : "");
        }
        public String[] getOptionsArray() {
            return (this.hasOptions() ? this.getOptions().split(";") : null);
        }
        public void setOptions(String options) { this.options = options; }
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
    }

    private String getExcelCellValue(Cell cell) {
        String value = "";
        if(cell != null) {
            switch(cell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    value = Boolean.toString(cell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if(HSSFDateUtil.isCellDateFormatted(cell)) {
                        value = CommonTool.convertTimestampToDate(cell.getDateCellValue());
                    } else {
                        value += cell.getNumericCellValue();
                        //remove '.0' portion for integer values
                        if(value.endsWith(".0")) {
                            value = value.substring(0, value.indexOf("."));
                        }
                    }
                    break;
                default:
                    value = cell.getStringCellValue();


            }
        }

        return value;
    }
}
