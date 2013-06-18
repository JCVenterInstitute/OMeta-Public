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

package org.jcvi.ometa.ontology;

import org.apache.log4j.Logger;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/10/13
 * Time: 4:16 PM
 */
public class OntologyLookupService {
    private Logger logger = Logger.getLogger(OntologyLookupService.class);

    public static final String ONTOLOGY_LS_URL = "http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=";

    private Map<String, String> ontologies;

    public OntologyLookupService() {
        ontologies = new TreeMap<String, String>();
    }

    private Query getOntologyQuery() throws Exception {
        return new QueryServiceLocator().getOntologyQuery();
    }

    public Map<String, String> getOntologies() {
        try {
            ontologies = this.getOntologyQuery().getOntologyNames();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return ontologies;
    }

    public Map<String, OntologyTerm> getRootTerms(String ontology) {
        Map<String, OntologyTerm> roots = null;
        try {
            roots = this.getOntologyQuery().getRootTerms(ontology);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return roots;
    }

    public String getTermById(String termId, String ontology) {
        String term = null;
        try {
            term = this.getOntologyQuery().getTermById(termId, ontology);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return term;
    }

    public Map<String, OntologyTerm> searchTerms(String searchWord, String ontology, boolean reverse) {
        Map<String, OntologyTerm> terms = null;
        try {
            terms = this.convertToOntologyMap(this.getOntologyQuery().getTermsByName(searchWord, ontology, reverse));
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return terms;
    }

    public Map<String, OntologyTerm> getParents(String termId, String ontology) {
        Map<String, OntologyTerm> parents = null;
        try {
            parents = this.convertToOntologyMap(this.getOntologyQuery().getTermParents(termId, ontology));
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return parents;
    }

    public Map<String, OntologyTerm> getParentsToRoot(String termId, String ontology) {
        Map<String, OntologyTerm> parents = new LinkedHashMap<String, OntologyTerm>();
        try {
            Map<String, OntologyTerm> directParents = this.getParents(termId, ontology);
            for(Map.Entry<String, OntologyTerm> entry : directParents.entrySet()) {
                parents.putAll(getParentsToRoot(entry.getKey(), entry.getValue().getOntology_a()));
            }
            parents.putAll(directParents);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return parents;
    }

    public Map<String, OntologyTerm> getChildren(String termId, String ontology) {
        Map<String, OntologyTerm> children = null;
        try {
            children = this.convertToOntologyMap(this.getOntologyQuery().getTermChildren(termId, ontology, 1, null));
            Map<String, String> relationships = this.getRelationships(termId, ontology);
            for(OntologyTerm term : children.values()) {
                term.setParentTermId(termId);
                term.setRelationship(relationships.get(term.getTermId()));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return children;
    }

    public Map<String, String> getRelationships(String termId, String ontology) {
        Map<String, String> relations = null;
        try {
            relations = this.getOntologyQuery().getTermRelations(termId, ontology);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return relations;
    }

    private Map<String, OntologyTerm> convertToOntologyMap(Map<String, String> map) {
        Map<String, OntologyTerm> ontologyMap = new LinkedHashMap<String, OntologyTerm>();
        if(map!=null && map.size()>0) {
            for(Map.Entry<String, String> entry : map.entrySet()) {
                OntologyTerm term = new OntologyTerm(entry.getKey(), entry.getValue());
                ontologyMap.put(entry.getKey(), term);
            }
        }
        return ontologyMap;
    }
    public List<OntologyTerm> convertOntologyMapToList(Map<String, OntologyTerm> map) {
        List<OntologyTerm> list = new ArrayList<OntologyTerm>(map.size());
        for(Map.Entry<String, OntologyTerm> entry : map.entrySet()) {
            list.add(entry.getValue());
        }
        return list;
    }
}
