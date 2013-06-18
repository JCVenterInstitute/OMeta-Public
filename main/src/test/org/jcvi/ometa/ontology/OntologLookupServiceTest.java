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

import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/10/13
 * Time: 4:25 PM
 */
public class OntologLookupServiceTest {
    OntologyLookupService service = new OntologyLookupService();

    @Test
    public void getOntologies() {
        Map<String, String> ontologies = service.getOntologies();
        assertNotNull(ontologies);
        assertTrue(ontologies.keySet().size()>0);
        assertTrue(ontologies.keySet().contains("GO"));

        System.out.println("Getting ontology from EBI, total ontology count:"+ontologies.size());
    }

    @Ignore
    public void searchTerm() {
        String search = "";
        String ontology = "GO";
        System.out.printf("Searching terms matches '%s' under %s%n", search, ontology);
        Map<String, OntologyTerm> terms = service.searchTerms(search, ontology, false);
        assertTrue(terms!=null && terms.size()>0);
        System.out.printf("Total terms found: %d%n", terms.size());

        for(Map.Entry<String, OntologyTerm> term : terms.entrySet()) {
            assertEquals(ontology, term.getValue().getOntology_a());
            System.out.printf("tmerId-%s, term-%s%n", term.getValue().getTermId(), term.getValue().getTerm());
        }
    }

    @Ignore
    public void getParents() {
        String ontology = "GO";
        String termId = "GO:0031324";
        System.out.printf("Getting parents of a termId '%s' of '%s'%n", termId, ontology);
        Map<String, OntologyTerm> parents = service.getParentsToRoot(termId, ontology);
        assertTrue(parents != null && parents.size() > 0);

        for(Map.Entry<String, OntologyTerm> term : parents.entrySet()) {
            System.out.printf("Term ID - %s, desc - %s%n", term.getValue().getTermId(), term.getValue().getTerm());
        }
    }

    @Ignore
    public void getRelationshipsAndChildren() {
        String ontology = "GO";
        String termId = "GO:0031323";
        System.out.printf("Getting relationships of a termId '%s' of '%s'%n", termId, ontology);
        Map<String, String> relations = service.getRelationships(termId, ontology);
        assertTrue(relations != null && relations.size() > 0);
        System.out.printf("Getting children of a termId '%s' of '%s'%n", termId, ontology);
        Map<String, OntologyTerm> children = service.getChildren(termId, ontology);
        assertTrue(children != null && children.size() > 0);

        //assertEquals(relations.size(), children.size());
        for(Map.Entry<String, String> term : relations.entrySet()) {
            if(children.containsKey(term.getKey())) {
                System.out.printf("Term ID - %s, term - %s, relatioship - %s%n", term.getKey(), children.get(term.getKey()).getTerm(), term.getValue());
                assertEquals(term.getValue(), children.get(term.getKey()).getRelationship());
            } else {
                System.err.println(term.getKey());
            }
        }
    }

    @Ignore
    public void getTermById() {
        String ontology = "GO";
        String termId = "GO:0031324";
        System.out.println("Getting term by Id");
        String term = service.getTermById(termId, ontology);
        assertNotNull(term);
        System.out.printf("Term ID: %s, %s", termId, term);
    }

    @Test
    public void test() throws Exception {
    }
}
