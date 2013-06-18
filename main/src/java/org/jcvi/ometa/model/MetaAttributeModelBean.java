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

package org.jcvi.ometa.model;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Dec 27, 2010
 * Time: 5:58:56 PM
 *
 * Special interface to indicate that the model is a special "meta attribute". 
 */
public interface MetaAttributeModelBean extends ModelBean {
    void setProjectName(String projectName);

    /** Tells the name of the attribute. */
    String getAttributeName();
    void setAttributeName( String name );

    /** Constrains type of data to assume, among all data type supportable by the model. */
    String getDataType();

    /** Link to lookup table. */
    Long getNameLookupId();
    void setNameLookupId( Long lookupId );

    /** Does this attribute have to be given? */
    Boolean isRequired();
    Boolean isActive();

    String getOntology();
    void setOntology(String ontology);
    Integer getActiveDB();
    void setActiveDB(Integer active);
    Integer getRequiredDB();
    void setRequiredDB(Integer required);
    String getDesc();
    void setDesc(String desc);
    String getOptions();
    void setOptions(String options);
    String getLabel();
    void setLabel(String label);

    LookupValue getLookupValue();
}
