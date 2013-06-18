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

package org.jcvi.ometa.configuration;

import org.jcvi.ometa.model.*;
import org.jcvi.ometa.utils.TsvPreProcessingUtils;
import org.jtc.common.util.tsv.TsvMappedReader;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Oct 22, 2010
 * Time: 4:40:51 PM
 * <p/>
 * For loading thing that will ultimately become events, or things to which events apply.
 */
public class EventLoader {

    /**
     * Reads any kind of bean, given file for input, and class.
     *
     * @param file      to read information.
     * @param beanClass read into this.
     * @param <B>       type of bean.
     * @return list of beans of the type given.
     * @throws Exception thrown if exception during get phase.
     */
    public <B extends ModelBean> List<B> getGenericModelBeans(File file, Class<B> beanClass)
            throws Exception {

        List<B> beans = null;
        try {
            beans = new ArrayList<B>();
            beans = this.getBeans(file, beans, beanClass);

        } catch (Throwable ex) {
            String beanHint = file.getAbsolutePath() +
                    ", while being read for objects of class " + beanClass.getName() +
                    " failed with " + ex.getMessage();
            throw new Exception(beanHint);
        }

        return beans;

    }

    /**
     * Pull in the information.  All data in file are expected/assumed to produce the same type of bean.
     *
     * @param inputFile what to load.
     */
    public <B extends ModelBean> List<B> getBeans(File inputFile, List<B> beans, Class<B> beanClass)
            throws Exception {

        String inputFileName = inputFile.getName();
        BeanFactory factory = new BeanFactory(InputBeanType.getInputBeanType(inputFileName));

        // Assume the file contains right kind of data for this tye of bean.
        TsvPreProcessingUtils preProcessor = new TsvPreProcessingUtils();
        File processedFile = preProcessor.preProcessTsvFile(inputFile);
        TsvMappedReader rdr = new TsvMappedReader(processedFile);

        BeanPopulator populator = new BeanPopulator(beanClass);
        rdr.setColumnNames(populator.getDefaultHeaderSet());

        Map<String, String> row;
        while (null != (row = rdr.getRowValues())) {
            B nextBean = factory.getInstance(beans);
            // NOTE: all of the beans in the file are required to be the same type.
            populator.populateBean(row, nextBean);
        }
        preProcessor.eliminatePreProcessedFile(processedFile);

        return beans;
    }

    /**
     * Read all kinds of attribute beans.
     *
     * @param inputFile what to load.
     */
    public List<FileReadAttributeBean> getGenericAttributeBeans(File inputFile) throws Exception {

        List<FileReadAttributeBean> beans = new ArrayList<FileReadAttributeBean>();

        // Assume the file contains right kind of data for this tye of bean.
        TsvPreProcessingUtils preProcessor = new TsvPreProcessingUtils();
        File processedFile = preProcessor.preProcessTsvFile(inputFile);
        TsvMappedReader rdr = new TsvMappedReader(processedFile);

        BeanPopulator populator = new BeanPopulator(FileReadAttributeBean.class);
        rdr.setColumnNames(populator.getDefaultHeaderSet());

        Map<String, String> row;
        while (null != (row = rdr.getRowValues())) {
            FileReadAttributeBean nextBean = new FileReadAttributeBean();
            // NOTE: all of the beans in the file are required to be the same type.
            populator.populateBean(row, nextBean);
            beans.add(nextBean);
        }

        preProcessor.eliminatePreProcessedFile(processedFile);
        return beans;
    }

    public List<FileReadAttributeBean> getGenericAttributeBeansFromCsv(File inputFile) throws Exception {

        List<FileReadAttributeBean> beans = new ArrayList<FileReadAttributeBean>();

        // Assume the file contains right kind of data for this tye of bean.
        TsvPreProcessingUtils preProcessor = new TsvPreProcessingUtils();
        File processedFile = preProcessor.preProcessTsvFile(inputFile);
        TsvMappedReader rdr = new TsvMappedReader(processedFile);

        BeanPopulator populator = new BeanPopulator(FileReadAttributeBean.class);
        rdr.setColumnNames(populator.getDefaultHeaderSet());

        Map<String, String> row;
        while (null != (row = rdr.getRowValues())) {
            FileReadAttributeBean nextBean = new FileReadAttributeBean();
            // NOTE: all of the beans in the file are required to be the same type.
            populator.populateBean(row, nextBean);
            beans.add(nextBean);
        }

        preProcessor.eliminatePreProcessedFile(processedFile);
        return beans;
    }

    /**
     * Type-parameterized factory method, to build out model beans.
     */
    public static class BeanFactory {
        private InputBeanType inputBeanType;

        /**
         * Construct with info for criteria to chose type of object to create.
         */
        public BeanFactory(InputBeanType inputBeanType) {
            this.inputBeanType = inputBeanType;
        }

        /**
         * Create a bean of the type dictated by configured criteria.
         */
        public <B extends ModelBean> B getInstance(List<B> beans) {
            B bean = null;

            switch (inputBeanType) {
                case eventMetaAttribute:
                    bean = (B) new EventMetaAttribute();
                    break;
                case projectMetaAttributes:
                    bean = (B) new ProjectMetaAttribute();
                    break;
                case sampleMetaAttributes:
                    bean = (B) new SampleMetaAttribute();
                    break;
                case project:
                    bean = (B) new Project();
                    break;
                case sample:
                    bean = (B) new Sample();
                    break;
                case lookupValue:
                    bean = (B) new LookupValue();
                    break;
                default:
                    break;
            }

            beans.add(bean);
            return bean;
        }

    }


}
