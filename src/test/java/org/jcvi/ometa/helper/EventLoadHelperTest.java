package org.jcvi.ometa.helper;

import org.jcvi.ometa.PSTTestAbstract;
import org.jcvi.ometa.engine.MultiLoadParameter;
import org.jcvi.ometa.model.FileReadAttributeBean;
import org.jcvi.ometa.model.GridBean;
import org.jcvi.ometa.utils.Constants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: movence
 * Date: 10/30/14
 * Time: 1:38 PM
 * org.jcvi.ometa.helper
 */
public class EventLoadHelperTest extends PSTTestAbstract {
    EventLoadHelper helper = new EventLoadHelper(this.getReadBean("dev"));

    @Before
    public void init() {

    }

    @Test
    public void main() throws Exception {
        //this.fileUpload1();
        //this.fileUpload2();
    }

    private void fileUpload1() throws Exception {
        String projectName = "DPCC_1000";
        String sampleName = "TEST1" + "-S1";

        FileReadAttributeBean bean = new FileReadAttributeBean();

        String filePath = "/Users/movence/works/test/ometa/seq/data.csv";
        File testFile = new File(filePath);

        bean.setUpload(testFile);
        bean.setUploadFileName(testFile.getName());

        bean.setAttributeName("Image");
        bean.setAttributeValue(filePath);

        bean.setProjectName(projectName);
        bean.setSampleName(sampleName);

        List<FileReadAttributeBean> beanList = new ArrayList<FileReadAttributeBean>();
        beanList.add(bean);

        List<GridBean> gridList = new ArrayList<GridBean>();
        GridBean gBean = new GridBean();
        gBean.setProjectName(projectName);
        gBean.setSampleName(sampleName);
        gBean.setBeanList(beanList);
        gridList.add(gBean);

        MultiLoadParameter multiParam = new MultiLoadParameter();
        helper.gridListToMultiLoadParameter(multiParam, gridList, projectName, Constants.EVENT_SAMPLE_UPDATE, "edit", "hkim");

    }

    private void fileUpload2() throws Exception {
        FileReadAttributeBean bean = new FileReadAttributeBean();

        String filePath = "/Users/movence/works/test/ometa/files/indresh1.csv";
        File testFile = new File(filePath);

        bean.setUpload(testFile);
        bean.setUploadFileName(testFile.getName());

        bean.setAttributeName("Sample Image");
    }
}
