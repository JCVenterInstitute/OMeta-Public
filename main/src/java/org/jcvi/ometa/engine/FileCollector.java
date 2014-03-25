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

package org.jcvi.ometa.engine;

import org.jcvi.ometa.configuration.FileMappingSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 3/2/11
 * Time: 3:33 PM
 *
 * Break up all types of files out of a list..
 */
public class FileCollector {
    /*
    protected static final String PROJ_REG_NAME_END = "ProjectRegistration_" + FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX;
    protected static final String SAMP_REG_NAME_END = "SampleRegistration_" + FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX;
    */

    private File[] files;
    /** Give something to test against, in c'tor. */
    public FileCollector(File[] files) {
        this.files = files;
        if(files == null) {
            throw new RuntimeException("Do not call with null file array.");
        }
    }

    public FileCollector(File directory) {
        if(directory.isDirectory() && directory.canRead()) {
            files = directory.listFiles();
        } else {
            throw new RuntimeException("Failed to read files from " + directory);
        }
    }

    public List<File> getProjectFiles() {
        return getFilesWhoseNamesEndWith(FileMappingSupport.PROJECT_FILE_SUFFIX);
    }
    public List<File> getSampleFiles() {
        return getFilesWhoseNamesEndWith(FileMappingSupport.SAMPLE_FILE_SUFFIX);
    }

    public List<File> getLookupValueFiles() {
        return getFilesWhoseNamesEndWith(FileMappingSupport.LOOKUPVALUE_FILE_SUFFIX);
    }

    public List<File> getProjectMetaAttributeFiles() {
        return getFilesWhoseNamesEndWith(FileMappingSupport.PROJECT_META_ATTRIBUTES_FILE_SUFFIX);
    }

    public List<File> getSampleMetaAttributeFiles() {
        return getFilesWhoseNamesEndWith(FileMappingSupport.SAMPLE_META_ATTRIBUTES_FILE_SUFFIX);
    }

    public List<File> getEventMetaAttributeFiles() {
        return getFilesWhoseNamesEndWith(FileMappingSupport.EVENT_META_ATTRIBUTES_FILE_SUFFIX);
    }

    private List<File> getFilesWhoseNamesEndWith(String nameEnd) {
        List<File> rtnfiles = makeEmptyList();
        for(File f: files) {
            if(f.getName().toLowerCase().endsWith(nameEnd.toLowerCase())) {
                rtnfiles.add(f);
            }
        }
        return rtnfiles;
    }

    /*
    public List<File> getProjectRegistrationFiles() {
        return getFilesWhoseNamesEndWith(PROJ_REG_NAME_END);
    }
    public List<File> getSampleRegistrationFiles() {
        return getFilesWhoseNamesEndWith(SAMP_REG_NAME_END);
    }
    public List<File> getEventFiles() {
        return getNonRegistrationEventFiles();
    }

    private List<File> getNonRegistrationEventFiles() {
        List<File> rtnfiles = makeEmptyList();
        for(File f: files) {
            if(f.getName().endsWith(FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX)  &&
                !f.getName().endsWith(PROJ_REG_NAME_END)  &&
                !f.getName().endsWith(SAMP_REG_NAME_END)
              ) {

                rtnfiles.add(f);
            }
        }
        return rtnfiles;
    }*/

    private List<File> makeEmptyList() { return new ArrayList<File>(); };
}
