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

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 4/25/11
 * Time: 11:48 AM
 *
 * Helps to categorize input types for processing.
 */
public enum InputBeanType {
    eventMetaAttribute      ( FileMappingSupport.EVENT_META_ATTRIBUTES_FILE_SUFFIX ),
    projectMetaAttributes   ( FileMappingSupport.PROJECT_META_ATTRIBUTES_FILE_SUFFIX ),
    sampleMetaAttributes    ( FileMappingSupport.SAMPLE_META_ATTRIBUTES_FILE_SUFFIX ),
    eventAttributes         ( FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX ),
    project                 ( FileMappingSupport.PROJECT_FILE_SUFFIX ),
    sample                  ( FileMappingSupport.SAMPLE_FILE_SUFFIX ),
    lookupValue             ( FileMappingSupport.LOOKUPVALUE_FILE_SUFFIX ),
    dictionary              ( FileMappingSupport.DICTIONARY_FILE_SUFFIX );

    private String beanFileSuffix;

    public static InputBeanType getInputBeanType( String inputFilePathStr ) {
        // NOTE: order below is important.
        if ( inputFilePathStr.endsWith( FileMappingSupport.PROJECT_FILE_SUFFIX ) ) {
            return project;
        } else if ( inputFilePathStr.endsWith( FileMappingSupport.SAMPLE_FILE_SUFFIX ) ) {
            return sample;
        } else if ( inputFilePathStr.toLowerCase().endsWith(FileMappingSupport.LOOKUPVALUE_FILE_SUFFIX.toLowerCase()) ) {
            return lookupValue;
        } else if ( inputFilePathStr.endsWith( FileMappingSupport.SAMPLE_META_ATTRIBUTES_FILE_SUFFIX ) ) {
            return sampleMetaAttributes;
        } else if ( inputFilePathStr.endsWith( FileMappingSupport.EVENT_META_ATTRIBUTES_FILE_SUFFIX ) ) {
            return eventMetaAttribute;
        } else if ( inputFilePathStr.endsWith( FileMappingSupport.PROJECT_META_ATTRIBUTES_FILE_SUFFIX ) ) {
            return projectMetaAttributes;
        } else if ( inputFilePathStr.endsWith( FileMappingSupport.EVENT_ATTRIBUTES_FILE_SUFFIX  ) ) {
            return eventAttributes;
        } else if ( inputFilePathStr.endsWith( FileMappingSupport.DICTIONARY_FILE_SUFFIX )){
            return dictionary;
        } else {
            throw new IllegalArgumentException( inputFilePathStr + " is of unknown file type" );
        }
    }

    public String getBeanFileSuffix() {
        return beanFileSuffix;
    }

    InputBeanType( String beanFileSuffix ) {
        this.beanFileSuffix = beanFileSuffix;
    }

}
