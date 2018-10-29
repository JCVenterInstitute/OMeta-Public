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

import org.hibernate.Session;
import org.jcvi.ometa.hibernate.dao.ProjectDAO;
import org.jcvi.ometa.hibernate.dao.SampleDAO;
import org.jcvi.ometa.hibernate.dao.StandaloneSessionAndTransactionManager;
import org.jcvi.ometa.model.Project;
import org.jcvi.ometa.model.Sample;
import org.jtc.common.util.property.PropertyHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/10/13
 * Time: 3:17 PM
 */
public class FileLoader {
    private final String NON_PUBLIC_PROJECT_SQL = "SELECT projet_name, projet_id FROM ifx_projects.project where project.projet_is_public=0";
    private final String NON_PUBLIC_SAMPLE_SQL = "SELECT sample_name FROM ifx_projects.sample where sample_projet_id=%s sample_is_public=0";

    Properties props;
    public FileLoader() {
        props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
    }

    public static void main(String[] arg) {
        FileLoader fileLoader = new FileLoader();
        fileLoader.process();
    }

    public void process() {
        try {
            String tarexcludePath = props.getProperty(Constants.CONFIG_TAREXCLUDE_PATH);
            String fileStoragePath = props.getProperty(Constants.CONIFG_FILE_STORAGE_PATH);
            String directoryFormat = "./data/%s/%s" + System.getProperty("line.separator");

            File tempFile = new File(tarexcludePath + ".temp");
            FileWriter fwriter = new FileWriter(tempFile);
            BufferedWriter bwriter = new BufferedWriter(fwriter);

            StandaloneSessionAndTransactionManager sessionManager = new StandaloneSessionAndTransactionManager(props, Constants.PRODUCTION_DATABASE);
            Session session = sessionManager.getSession();
            sessionManager.startTransaction();

            ProjectDAO projectDao = new ProjectDAO();
            SampleDAO sampleDao = new SampleDAO();

            List<Project> projects = projectDao.getProjectsByPublicFlag(false, session);
            File directory = null;
            for(Project project : projects) {
                List<Sample> samples = sampleDao.getSamplesByPublicFlag(project.getProjectId(), false, session);
                String unspacedPorjectName = project.getProjectName().replaceAll(" ", "_"); //project folder
                for(Sample sample : samples) {
                    String unspacedSampleName = sample.getSampleName().replaceAll(" ", "_"); //sample folder
                    directory = new File(fileStoragePath + unspacedPorjectName + File.separator + unspacedSampleName);
                    if(directory.exists() && directory.isDirectory()) {
                        bwriter.write(String.format(directoryFormat, unspacedPorjectName, unspacedSampleName));
                    }
                }

                directory = new File(fileStoragePath + unspacedPorjectName + File.separator + "project");
                if(directory.exists() && directory.isDirectory()) {
                    bwriter.write(String.format(directoryFormat, unspacedPorjectName, "project"));
                }
            }

            sessionManager.commitTransaction();
            sessionManager.closeSession();

            bwriter.close();
            fwriter.close();

            if(tempFile.exists() && tempFile.isFile()) {
                File realFile = new File(tarexcludePath);
                tempFile.renameTo(realFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        }
    }
}
