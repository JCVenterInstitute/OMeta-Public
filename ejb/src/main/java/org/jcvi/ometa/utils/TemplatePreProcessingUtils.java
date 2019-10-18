package org.jcvi.ometa.utils;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.*;
import org.jcvi.ometa.model.*;
import org.jcvi.ometa.validation.ErrorMessages;
import org.jcvi.ometa.validation.ModelValidator;
import org.jtc.common.util.scratch.ScratchUtils;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

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
    private final String mutatedComment = Constants.TEMPLATE_COMMENT_INDICATOR.concat("%s %s");

    public InputStream buildFileContent(String type, List<EventMetaAttribute> emas, String projectName, String sampleName, String eventName) throws Exception {

        boolean isProjectRegistration = eventName.contains(Constants.EVENT_PROJECT_REGISTRATION);
        //boolean isProjectUpdate = eventName.replaceAll("\\s","").equals("ProjectUpdate");
        boolean isEventRegistration = eventName.contains(Constants.EVENT_REGISTRATION);

        List<HeaderDetail> headers = new ArrayList<>();

        headers.add(new HeaderDetail(Constants.ATTR_PROJECT_NAME, true, "string", "", null));


        if (isEventRegistration) { // parent sample name for sample registration
            headers.add(new HeaderDetail(Constants.ATTR_PARENT_SAMPLE_NAME, false, "string", "", null));
        }

        if (isProjectRegistration || isEventRegistration) { //public flag
            headers.add(new HeaderDetail(Constants.ATTR_PUBLIC_FLAG, true, "int", "", null));
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

        //if(!isEventRegistration && sampleRequired) { //remove sample name for sample registration
        if(isEventRegistration || sampleRequired) {
            headers.add(1, new HeaderDetail(Constants.ATTR_SAMPLE_NAME, true, "string", "", null));
        }

        InputStream templateStream;
        if(type.equals("e")) {
            templateStream = this.createExcel(headers, isProjectRegistration, projectName, sampleName, eventName);
        } else {
            templateStream = this.createCSV(headers, isProjectRegistration, projectName, sampleName, eventName);
        }

        return templateStream;
    }

    public InputStream buildProjectSetupContent(Project project, List<ProjectMetaAttribute> pmaList, List<EventMetaAttribute> emaList,
                                                List<LookupValue> eventNameList, List<SampleMetaAttribute> smaList) {
        StringBuilder csvContents = new StringBuilder();
        String projectName = project.getProjectName();

        //Project Setup
        csvContents.append(Constants.TEMPLATE_COMMENT_INDICATOR).append(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER).append(":").append("Project\n");
        csvContents.append(Constants.ATTR_PROJECT_NAME).append(",")
                .append(Constants.ATTR_PARENT_PROJECT_NAME).append(",")
                .append(Constants.ATTR_PROJECT_LEVEL).append(",")
                .append(Constants.ATTR_PUBLIC_FLAG).append("\n");
        csvContents.append(projectName + "," + project.getParentProjectName() + ","
                + project.getProjectLevel() + "," + project.getIsPublic() + "\n");

        csvContents.append("\n");

        //Project Meta Attributes
        csvContents.append(Constants.TEMPLATE_COMMENT_INDICATOR).append(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER).append(":").append("ProjectMetaAttributes\n");
        csvContents.append(Constants.ATTR_PROJECT_NAME).append(",")
                .append(Constants.ATTR_LABEL).append(",")
                .append(Constants.ATTR_DATA_TYPE).append(",")
                .append(Constants.ATTR_REQUIRED).append(",")
                .append(Constants.ATTR_ATTRIBUTE_NAME).append(",")
                .append(Constants.ATTR_DESCRIPTION).append(",")
                .append(Constants.ATTR_OPTIONS).append(",")
                .append(Constants.ATTR_ORDER).append("\n");
        int i = 0;
        for(ProjectMetaAttribute pma : pmaList){
            csvContents.append("\"").append(projectName).append("\",")
                    .append("\"").append(pma.getLabel()).append("\",")
                    .append("\"").append(pma.getDataType()).append("\",")
                    .append("\"").append(pma.isRequired() ? "T" : "F").append("\",")
                    .append("\"").append(pma.getAttributeName()).append("\",")
                    .append("\"").append(pma.getDesc()).append("\",")
                    .append("\"").append(pma.getOptions()).append("\",")
                    .append(++i).append("\n");
        }

        csvContents.append("\n");

        //Sample Meta Attributes
        csvContents.append(Constants.TEMPLATE_COMMENT_INDICATOR).append(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER).append(":").append("SampleMetaAttributes\n");
        csvContents.append(Constants.ATTR_PROJECT_NAME).append(",")
                .append(Constants.ATTR_LABEL).append(",")
                .append(Constants.ATTR_DATA_TYPE).append(",")
                .append(Constants.ATTR_REQUIRED).append(",")
                .append(Constants.ATTR_ATTRIBUTE_NAME).append(",")
                .append(Constants.ATTR_DESCRIPTION).append(",")
                .append(Constants.ATTR_OPTIONS).append(",")
                .append(Constants.ATTR_ORDER).append("\n");
        i=0;
        for(SampleMetaAttribute sma : smaList){
            csvContents.append("\"").append(projectName).append("\",")
                    .append("\"").append(sma.getLabel()).append("\",")
                    .append("\"").append(sma.getDataType()).append("\",")
                    .append("\"").append(sma.isRequired() ? "T" : "F").append("\",")
                    .append("\"").append(sma.getAttributeName()).append("\",")
                    .append("\"").append(sma.getDesc()).append("\",")
                    .append("\"").append(sma.getOptions()).append("\",")
                    .append(++i).append("\n");
        }

        csvContents.append("\n");

        //Project Meta Attributes
        csvContents.append(Constants.TEMPLATE_COMMENT_INDICATOR).append(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER).append(":").append("EventMetaAttributes\n");
        csvContents.append(Constants.ATTR_PROJECT_NAME).append(",")
                .append(Constants.ATTR_EVENT_NAME).append(",")
                .append(Constants.ATTR_SAMPLE_REQUIRED).append(",")
                .append(Constants.ATTR_LABEL).append(",")
                .append(Constants.ATTR_DATA_TYPE).append(",")
                .append(Constants.ATTR_REQUIRED).append(",")
                .append(Constants.ATTR_ATTRIBUTE_NAME).append(",")
                .append(Constants.ATTR_DESCRIPTION).append(",")
                .append(Constants.ATTR_OPTIONS).append(",")
                .append(Constants.ATTR_ORDER).append("\n");
        Map<String, StringBuilder> emaMap = new HashMap<>(eventNameList.size());
        for(LookupValue lv : eventNameList){
            emaMap.put(lv.getName(), new StringBuilder());
        }

        for(EventMetaAttribute ema : emaList){
            String eventName = ema.getEventName();
            emaMap.get(eventName).append("\"").append(projectName).append("\",")
                    .append("\"").append(eventName).append("\",")
                    .append("\"").append(ema.isSampleRequired() ? "T" : "F").append("\",")
                    .append("\"").append(ema.getLabel()).append("\",")
                    .append("\"").append(ema.getDataType()).append("\",")
                    .append("\"").append(ema.isRequired() ? "T" : "F").append("\",")
                    .append("\"").append(ema.getAttributeName()).append("\",")
                    .append("\"").append(ema.getDesc()).append("\",")
                    .append("\"").append(ema.getOptions()).append("\",")
                    .append(ema.getOrder()).append("\n");
        }

        for(LookupValue lv : eventNameList){
            csvContents.append(emaMap.get(lv.getName()));
        }

        return IOUtils.toInputStream(csvContents.toString().replaceAll("null", ""));
    }

    private InputStream createCSV(List<HeaderDetail> attributes, boolean isProjectRegistration, String projectName, String sampleName, String eventName) throws Exception {
        StringBuilder csvContents = new StringBuilder();
        StringBuilder comments = new StringBuilder();

        csvContents.append(Constants.TEMPLATE_COMMENT_INDICATOR).append(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER).append(":").append(eventName).append("\n"); //write the event name with the identifier

        int i = 0;
        for(HeaderDetail detail : attributes) {
            if(i > 0) {
                csvContents.append(",");
                comments.append(",");
            }
            csvContents.append(detail.getDisplayHeader());
            comments.append("\"" + this.getComment(detail) + (detail.hasOptions() ? detail.getOptionsString() : "") + "\"");
            i++;
        }

        csvContents.append("\n" + comments.toString());

        csvContents.append("\n" +
                        (!isProjectRegistration ? projectName : "") +
                        (sampleName != null && !sampleName.trim().isEmpty() ? "," + sampleName : "")
        );
        return IOUtils.toInputStream(csvContents.toString());
    }

    private InputStream createExcel(List<HeaderDetail> attributes, boolean isProjectRegistration, String projectName, String sampleName, String eventName) throws Exception {
        Workbook wb = new HSSFWorkbook();
        //CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet(eventName);

        CellStyle boldCS = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setFontHeightInPoints((short) 12);
        boldFont.setBold(true);
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
        Cell cell;
        for(HeaderDetail detail : attributes) {
            cell = attributeRow.createCell(headerIndex);
            cell.setCellValue(detail.getDisplayHeader());
            cell.setCellStyle(boldCS);

            cell = commentRow.createCell(headerIndex++);
            cell.setCellValue(this.getComment(detail));
            cell.setCellStyle(redCS);

            if(detail.getDataType().equals(Constants.DATE_DATA_TYPE)) {
                CellStyle dateCS = wb.createCellStyle();
                CreationHelper createHelper = wb.getCreationHelper();
                dateCS.setDataFormat(createHelper.createDataFormat().getFormat(Constants.DATE_DEFAULT_FORMAT));

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
        return String.format(this.mutatedComment, detail.getDataType(), detail.isRequired() ? "*Required" : "optional");
    }

    private void addValidations(int headerIndex, HeaderDetail detail, Sheet sheet) {
        CellRangeAddressList addressList = new CellRangeAddressList(2, 100, headerIndex, headerIndex);
        DVConstraint constraint;
        DataValidation validation;
        //adds select box with option values
        if(detail.hasOptions()) {
            constraint = DVConstraint.createExplicitListConstraint(detail.getOptionsArray());
            validation = new HSSFDataValidation(addressList, constraint);
            validation.setSuppressDropDownArrow(false);
            sheet.addValidationData(validation);
        }

        //data type validation
        if(detail.getDataType().equals(Constants.DATE_DATA_TYPE)) {
            constraint = DVConstraint.createDateConstraint(
                    DVConstraint.OperatorType.GREATER_THAN,
                    "1900-01-01", "0000-00-00",
                    Constants.DATE_DEFAULT_FORMAT
            );
            validation = new HSSFDataValidation(addressList, constraint);
            validation.setSuppressDropDownArrow(true);
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("Use a valid date format!", Constants.DATE_DEFAULT_FORMAT);
            sheet.addValidationData(validation);
        } else if(detail.getDataType().equals(Constants.INT_DATA_TYPE)) {
            constraint = DVConstraint.createNumericConstraint(
                    DVConstraint.ValidationType.INTEGER,
                    DVConstraint.OperatorType.GREATER_OR_EQUAL,
                    "0", "0"
            );
            validation = new HSSFDataValidation(addressList, constraint);
            validation.setSuppressDropDownArrow(true);
            sheet.addValidationData(validation);
        } else if(detail.getDataType().equals(Constants.FLOAT_DATA_TYPE)) {
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

    public List<GridBean> parseEventFile(String originalFileName, File uploadedFile, String projectName, boolean isProjectRegistration, boolean isRegistration) throws Exception {
        return parseEventFile(originalFileName, uploadedFile, projectName, isProjectRegistration, isRegistration, false, false);
    }

    public List<GridBean> parseEventFile(String originalFileName, File uploadedFile, String projectName, boolean isProjectRegistration, boolean isEventRegistration,
                                         boolean isVisitEvent, boolean isSampleEvent) throws Exception {
        List<GridBean> gridBeans = new ArrayList<>();

        String fileType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

        List<String> columns = new ArrayList<>();

        String currProjectName;

        boolean hasSampleName = false;
        boolean hasParentSampleName = false;
        boolean hasPublicFlag = false;

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
                        columns.add(this.extractRealAttributeName(attributeNames.getCell(i).getStringCellValue()));
                    }
                    hasSampleName = columns.indexOf(Constants.ATTR_SAMPLE_NAME) >= 0;
                    hasParentSampleName = columns.indexOf(Constants.ATTR_PARENT_SAMPLE_NAME) >= 0;
                    hasPublicFlag = columns.indexOf(Constants.ATTR_PUBLIC_FLAG) >= 0;

                    int startingRow = 1;
                    Row metaRow = sheet.getRow(startingRow);
                    String firstMetaColumn = metaRow.getCell(0).getStringCellValue();
                    if(!firstMetaColumn.isEmpty() && firstMetaColumn.startsWith(Constants.TEMPLATE_COMMENT_INDICATOR) && firstMetaColumn.indexOf("string") > 0) {
                        startingRow = 2; //skip the second line that holds metadata of each column
                    }


                    for(int i = startingRow; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        int colIndex = 0;

                        currProjectName = row.getCell(colIndex++).getStringCellValue();
                        if(!isProjectRegistration && !currProjectName.equals(projectName)) {
                            throw new Exception(ErrorMessages.TEMPLATE_MULTIPLE_PROJECT);
                        }

                        GridBean gBean = new GridBean();
                        gBean.setProjectName(currProjectName);

                        if(hasSampleName) {
                            gBean.setSampleName(this.getExcelCellValue(row.getCell(colIndex++)));
                        }

                        if(isProjectRegistration) {
                            gBean.setProjectName(currProjectName);
                            if(hasPublicFlag) {
                                gBean.setProjectPublic(this.getExcelCellValue(row.getCell(colIndex++)));
                            }
                        } else if(isEventRegistration) {
                            if(hasSampleName){
                                if(hasParentSampleName) {
                                    gBean.setParentSampleName(this.getExcelCellValue(row.getCell(colIndex++)));
                                }
                                if(hasPublicFlag) {
                                    gBean.setSamplePublic(this.getExcelCellValue(row.getCell(colIndex++)));
                                }
                            }
                        }

                        gBean.setBeanList(new ArrayList<>());
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

            try (CSVReader reader = new CSVReader(new FileReader(uploadedFile))) {
                String[] line;
                int lineCount = 0;
                while ((line = reader.readNext()) != null) {
                    ++lineCount;

                    if (lineCount == 1) { // event name header
                        if (line[0].startsWith(Constants.TEMPLATE_COMMENT_INDICATOR) && line[0].contains(Constants.TEMPLATE_EVENT_TYPE_IDENTIFIER)) { //skip event type line
                            continue;
                        } else {
                            throw new Exception(ErrorMessages.TEMPLATE_MISSING_HEADER_EVENT);
                        }

                    } else if (lineCount == 2) { // attribute headers
                        Collections.addAll(columns, line);
                        hasSampleName = columns.indexOf(Constants.ATTR_SAMPLE_NAME) >= 0;
                        hasParentSampleName = columns.indexOf(Constants.ATTR_PARENT_SAMPLE_NAME) >= 0;
                        hasPublicFlag = columns.indexOf(Constants.ATTR_PUBLIC_FLAG) >= 0;

                    } else { // data lines
                        int colIndex = 0;

                        if (line.length < 1) { // skip empty line
                            continue;
                        }

                        if (lineCount > Constants.TEMPLATE_MAX_ROW_LIMIT) { // oversize template check
                            throw new Exception(ErrorMessages.TEMPLATE_OVERSIZE);
                        }

                        if (line[0].startsWith(Constants.TEMPLATE_COMMENT_INDICATOR) || line[0].startsWith("\"" + Constants.TEMPLATE_COMMENT_INDICATOR)) { //skip comment line
                            continue;
                        }

                        if (lineCount == 3) {
                            //skip the second line that holds metadata of each column
                            String firstMetaColumn = line[colIndex];
                            if (!firstMetaColumn.isEmpty() && firstMetaColumn.startsWith(Constants.TEMPLATE_COMMENT_INDICATOR) && firstMetaColumn.indexOf("string") > 0) {
                                continue;
                            }
                        }

                        if (line.length != columns.size()) {
                            throw new Exception(ErrorMessages.TEMPLATE_COLUMN_COUNT_MISMATCH);
                        }

                        currProjectName = line[colIndex++];
                        if (currProjectName == null || currProjectName.isEmpty()) {
                            throw new Exception(ErrorMessages.TEMPLATE_PROJECT_MISSING);
                        }

                        if (projectName == null) { //assign the first project
                            projectName = currProjectName;
                        }

                        if (!currProjectName.isEmpty()) {
                            if (!isProjectRegistration && !currProjectName.equals(projectName)) {
                                throw new Exception(ErrorMessages.TEMPLATE_MULTIPLE_PROJECT);
                            }

                            GridBean gBean = new GridBean();
                            gBean.setProjectName(currProjectName);

                            if (hasSampleName) {
                                gBean.setSampleName(line[(colIndex++)]);
                            }

                            if (isProjectRegistration) {
                                gBean.setProjectName(currProjectName);
                                if (hasPublicFlag) {
                                    gBean.setProjectPublic(line[(colIndex++)]);
                                }
                            } else if (isEventRegistration) {
                                if (hasSampleName) {
                                    if (hasParentSampleName) {
                                        gBean.setParentSampleName(line[(colIndex++)]);
                                    }
                                    if (hasPublicFlag) {
                                        gBean.setSamplePublic(line[(colIndex++)]);
                                    }
                                }
                            }

                            gBean.setBeanList(new ArrayList<>());
                            for (; colIndex < columns.size(); colIndex++) {
                                FileReadAttributeBean fBean = new FileReadAttributeBean();
                                fBean.setProjectName(isProjectRegistration ? currProjectName : projectName);
                                fBean.setSampleName(hasSampleName ? gBean.getSampleName() : null);
                                String attributeName = this.extractRealAttributeName(columns.get(colIndex));
                                fBean.setAttributeName(attributeName);
                                fBean.setAttributeValue(line[colIndex]);
                                //autoconcat sample id based on parent id and visit date/specimen type
                                if (isVisitEvent && attributeName.equalsIgnoreCase("Visit Date"))
                                    gBean.setSampleName(gBean.getParentSampleName() + '_' + line[colIndex].replaceAll("-", ""));
                                else if (isSampleEvent && attributeName.equalsIgnoreCase("sample type"))
                                    gBean.setSampleName(gBean.getParentSampleName() + '_' + CommonTool.firstPatternMatch(line[colIndex], "\\((.*?)\\)"));

                                gBean.getBeanList().add(fBean);
                            }

                            gBean.setParsedRowData(line);

                            gridBeans.add(gBean);
                        }
                    }
                }
            }
        }

        return gridBeans;
    }

    /**
     * parse project, sample, meta attribute files into a list of maps
     * @param beanFile
     * @return list of maps containing field name and value pairs
     * @throws Exception
     */
    public List<Map<String, String>> parseNonEventFile(File beanFile) throws Exception {
        List<Map<String, String>> dataList = new ArrayList<>();

        CSVReader reader = new CSVReader(new FileReader(beanFile));

        String[] line;
        int lineCount = 0;
        List<String> columns = new ArrayList<>();

        while((line = reader.readNext()) != null) {
            if(lineCount == 0) { //headers
                Collections.addAll(columns, line);
            } else {

                Map<String, String> data = new HashMap<>();
                for(int i = 0;i < columns.size();i++) {
                    data.put(columns.get(i), line[i]);
                }
                dataList.add(data);
            }
            lineCount++;
        }

        return dataList;
    }


    public File preProcessTemplateFile(File originalFile) {
        File outputFile;
        try {
            // Setup scratch location.
            String userBase = System.getProperty("user.home");
            ScratchUtils.setScratchBaseLocation(userBase + "/" + Constants.SCRATCH_BASE_LOCATION);
            Long timeStamp = new Date().getTime();
            File scratchLoc = ScratchUtils.getScratchLocation(timeStamp, "EventLoader__" + originalFile.getName());

            outputFile = new File( scratchLoc, originalFile.getName() );

            BufferedReader br = new BufferedReader( new FileReader( originalFile ) );
            PrintWriter pw = new PrintWriter( new FileWriter( outputFile ) );

            String inline;
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

    /*
     * Will get rid of files created as temporaries by this process.
     * This method is copied over from original template processor, TsvPreProcessingUtils.java
     * 3/10/14 by hkim
    */
    public void deletePreProcessedFile( File file ) {
        try {
            if(file.exists()) {
                file.delete();
                File parentFile = file.getParentFile();
                // If parent exists, definitely should be deleted.  Separate directory
                // created for each temporary file.  But the grandparent should only be deleted
                // if it is just a numeric value.  Numerically-named directories may be
                // included as grandparents.
                if( parentFile.canWrite() ) {
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
            throw new IllegalArgumentException( ex );
        }
    }

    private String getExcelCellValue(Cell cell) throws Exception {
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

    private String extractRealAttributeName(String attributeHeader) {
        String realAttributeName = attributeHeader.trim();
        if(attributeHeader.contains("[") && attributeHeader.endsWith("]")) {
            realAttributeName = attributeHeader.substring(attributeHeader.indexOf("[") + 1, attributeHeader.indexOf("]"));
        }
        return realAttributeName;
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

        public String getDisplayHeader() { //append square brackets wrapped attribute name with a label
            String displayHeader = this.getName();
            //            Following lines commented for DPCC
            //            if(this.getLabel() != null && !this.getLabel().isEmpty()) {
            //                displayHeader = this.getLabel() + "[" + displayHeader + "]";
            //            }
            return displayHeader;
        }
    }
}
