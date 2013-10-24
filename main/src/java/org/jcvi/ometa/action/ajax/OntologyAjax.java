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

package org.jcvi.ometa.action.ajax;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.jcvi.ometa.ontology.OntologyLookupService;
import org.jcvi.ometa.ontology.OntologyTerm;
import uk.ac.ebi.ontocat.OntologyService;
import uk.ac.ebi.ontocat.OntologyServiceException;
import uk.ac.ebi.ontocat.bioportal.BioportalOntologyService;
import uk.ac.ebi.ontocat.ols.OlsOntologyService;
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/17/13
 * Time: 1:09 PM
 *
 * This feature is incomplete, since ontology service will be using ontoCAT
 * http://www.ontocat.org/
 */
public class OntologyAjax extends ActionSupport implements IAjaxAction {
    Logger logger = Logger.getLogger(OntologyAjax.class);
    private String ot;
    private String tid;
    private String sw;
    private boolean ex;
    private String t;

    private String ser;

    private List result;

    @Override
    public String runAjax() {
        String rtnVal = SUCCESS;

        result = new ArrayList();
        try {
            OntologyService oservice = null;
            if(ser!=null && ser.equalsIgnoreCase("bio")) {
                oservice = new BioportalOntologyService();
            } else {
                oservice = new OlsOntologyService();

            }
            org.jcvi.ometa.ontology.BioportalOntologyService bioportal = new org.jcvi.ometa.ontology.BioportalOntologyService();

            //OntologyLookupService ols = new OntologyLookupService();

            if(t.equals("ot")) { //get ontologies
                OntologyLookupService ols = new OntologyLookupService();
                result.add(ols.getOntologies());
                //result = ontologyService.getOntologies();
            } else if(t.equals("root")) { //root terms
                result = oservice.getRootTerms(ot);
            } else if(t.equals("child")) { //children
                //result = oservice.getChildren(ot, tid);

                List<org.jcvi.ometa.ontology.OntologyTerm> terms = bioportal.search(sw, tid, ot); //get all descendants from a term
                if(terms!=null && terms.size()>0) {
                    for(OntologyTerm term : terms) {
                        Map<String, Object> ot = new HashMap<String, Object>();
                        ot.put("ontology", term.getOntologyId());
                        ot.put("ontolabel", term.getOntologyFull());
                        ot.put("taccession", term.getTermId());
                        ot.put("tlabel", term.getTerm());
                        ot.put("uri", null);
                        result.add(ot);
                    }
                }

            } else if(t.equals("pare")) { //parents
                result = oservice.getParents(ot, tid);
            } else if(t.equals("term")) { //get term
                result.add(oservice.getTerm(ot, tid));
            } else if(t.equals("rela")) { //relationship
                result.add(oservice.getRelations(ot, tid));
            } else if(t.equals("anno")) { //annotations
                result.add(oservice.getAnnotations(ot, tid));
            } else if(t.equals("syno")) { //synonyms
                result = oservice.getSynonyms(ot, tid);
            } else if(t.equals("defi")) { //definition
                result = oservice.getDefinitions(ot, tid);
            } else if(t.equals("path")) { //path
                result = oservice.getTermPath(ot, tid);
            } else if(t.equals("srch")) { //search
                result = oservice.searchOntology(ot, sw);
            } else if(t.equals("sall")) { //search all
                /* straight use of ontocat library is too slow and unstable
                 * commented by hkim on 5/29/13
                result = oservice.searchAll(sw);
                */

                /* old version using OLS
                Map<String, String> ontologies = ols.getOntologies();

                QueryServiceLocator locator = new QueryServiceLocator();
                Query qs = locator.getOntologyQuery();

                Set<Map.Entry<String, String>> sTerms = qs.getPrefixedTermsByName(sw, false).entrySet();
                for (Map.Entry<String, String> entry : sTerms) {
                    // splitting e.g. 228975=NEWT:Thymus magnus
                    String termAccession = entry.getKey();
                    String ontologyAccession = entry.getValue().split(":")[0];
                    String label = entry.getValue().split(":")[1];
                    URI uri = URI.create("http://www.ebi.ac.uk/ontology-lookup/?termId=" + termAccession);
                    // filter out non-exact terms if necessary
                    if (ex && !label.equalsIgnoreCase(sw)) {
                        continue;
                    }
                    // Inject searchoptions into context
                    Map<String, Object> ot = new HashMap<String, Object>();
                    ot.put("ontology", ontologyAccession);
                    ot.put("ontolabel", ontologies.get(ontologyAccession));
                    ot.put("taccession", termAccession);
                    ot.put("tlabel", label);
                    ot.put("uri", uri);
                    result.add(ot);
                }
                */

                List<org.jcvi.ometa.ontology.OntologyTerm> terms = bioportal.search(sw, null, null);
                if(terms!=null && terms.size()>0) {
                    for(OntologyTerm term : terms) {
                        Map<String, Object> ot = new HashMap<String, Object>();
                        ot.put("ontology", term.getOntologyId());
                        ot.put("ontolabel", term.getOntologyFull());
                        ot.put("taccession", term.getTermId());
                        ot.put("tlabel", term.getTerm());
                        ot.put("uri", null);
                        result.add(ot);
                    }
                }


                /*
                //trim result size from growing too big
                int maxResultSize = 250;
                int originalSize = result.size();
                if(originalSize > maxResultSize) {
                    result = result.subList(0, maxResultSize);
                    result.add((originalSize-maxResultSize-1) + " more...try more descriptive search!");
                }*/
            }

        } catch (OntologyServiceException ose) {
            logger.error(ose.getMessage());
            rtnVal = ERROR;
        } catch (Exception e) {
            logger.error(e.getMessage());
            rtnVal = ERROR;
        }
        return rtnVal;
    }

    public void other() throws Exception {


    }

    public void searchAll() throws OntologyServiceException, javax.xml.rpc.ServiceException {
        QueryServiceLocator locator = new QueryServiceLocator();
        Query qs = locator.getOntologyQuery();

        try {
            Set<Map.Entry<String, String>> sTerms = qs.getPrefixedTermsByName(sw, false).entrySet();
            List<uk.ac.ebi.ontocat.OntologyTerm> result = new ArrayList<uk.ac.ebi.ontocat.OntologyTerm>();
            for (Map.Entry<String, String> entry : sTerms) {
                // splitting e.g. 228975=NEWT:Thymus magnus
                String termAccession = entry.getKey();
                String ontologyAccession = entry.getValue().split(":")[0];
                String label = entry.getValue().split(":")[1];
                URI uri = URI.create("http://www.ebi.ac.uk/ontology-lookup/?termId=" + termAccession);
                // filter out non-exact terms if necessary
                if (ex && !label.equalsIgnoreCase(sw)) {
                    continue;
                }
                // Inject searchoptions into context
                uk.ac.ebi.ontocat.OntologyTerm ot = new uk.ac.ebi.ontocat.OntologyTerm(ontologyAccession,termAccession, label, uri);
                result.add(ot);
            }
        } catch (RemoteException e) {
            throw new OntologyServiceException(e);
        }
    }

    public String getOt() {
        return ot;
    }

    public void setOt(String ot) {
        this.ot = ot;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getSw() {
        return sw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public List getResult() {
        return result;
    }

    public void setResult(List result) {
        this.result = result;
    }

    public String getSer() {
        return ser;
    }

    public void setSer(String ser) {
        this.ser = ser;
    }

    public boolean isEx() {
        return ex;
    }

    public void setEx(boolean ex) {
        this.ex = ex;
    }
}
