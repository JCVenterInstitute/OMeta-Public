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

package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.db_interface.ReadBeanPersister;
import org.jcvi.ometa.ontology.OntologyLookupService;
import org.jcvi.ometa.utils.Constants;
import org.jtc.common.util.property.PropertyHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/17/13
 * Time: 9:20 AM
 */
public class Ontology extends ActionSupport{
    private Logger logger = Logger.getLogger(Ontology.class);

    private OntologyLookupService ontologyService;
    private ReadBeanPersister readPersister;

    private List<String> ontologies;

    public Ontology() {
        ontologyService = new OntologyLookupService();

        Properties props = PropertyHelper.getHostnameProperties(Constants.PROPERTIES_FILE_NAME);
        readPersister = new ReadBeanPersister(props);
    }

    public String execute() {
        String rtnVal = SUCCESS;
        Map<String, String> ontologyMap = ontologyService.getOntologies();
        if(ontologyMap!=null && ontologyMap.size()>0) {
            ontologies = new ArrayList<String>();
            for(Map.Entry<String, String> entry : ontologyMap.entrySet()) {
                ontologies.add(String.format("%s (%s)", entry.getValue(), entry.getKey())); //make "ontology (abbreviation)"
            }
        }

        return rtnVal;
    }

    public List<String> getOntologies() {
        return ontologies;
    }

    public void setOntologies(List<String> ontologies) {
        this.ontologies = ontologies;
    }
}
