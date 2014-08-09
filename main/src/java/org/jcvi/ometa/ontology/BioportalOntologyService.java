package org.jcvi.ometa.ontology;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * User: hkim
 * Date: 10/2/13
 * Time: 11:31 AM
 * org.jcvi.ometa.ontology
 */
public class BioportalOntologyService {
    private final String bioportalAddress = "http://data.bioontology.org/";
    private final String bioportalApiKey = "a6b4dbc7-d18d-40ce-a5d0-93e5e5a88468";

    public BioportalOntologyService() {

    }

    public List<OntologyTerm> search(String search, String searchRootId, String ontologyId) throws Exception {
        String serviceUrl = this.buildUrl("search", search, searchRootId, ontologyId);
        String resultString = this.callService(serviceUrl);

        List<OntologyTerm> results = null;
        if(resultString!=null && !resultString.isEmpty()) {
            results = this.processSearchResult(resultString);
        }

        return results;
    }

    private String buildUrl(String type, String search, String searchRootId, String ontologyId) {
        // search?q=melanoma
        // search?q=intraocular&ontology=HP&subtree_root=http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2FHP_0002861

        StringBuilder urlBuilder = new StringBuilder(bioportalAddress);
        if(type.equals("search")) {
            urlBuilder.append("search/?q=" + search);
        }

        if(searchRootId!=null && !searchRootId.isEmpty() && ontologyId!=null && !ontologyId.isEmpty()) {
            urlBuilder.append("&ontology=" + ontologyId);
            urlBuilder.append("&subtree_root=" + URLEncoder.encode(ontologyId));
        }

        urlBuilder.append("&pagesize=150");
        urlBuilder.append("&include_context=false");
        urlBuilder.append("&include_views=true");
        urlBuilder.append("&require_definition=true");

        return urlBuilder.toString();
    }

    private String callService(String url) throws Exception {
        String result = null;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "apikey token=" + bioportalApiKey);
        con.setConnectTimeout(15000); //times out after 15 secs

        int responseCode = con.getResponseCode();
        if(responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            result = response.toString();
        }

        return result;
    }

    private List<OntologyTerm> processSearchResult(String result) throws Exception {
        List<OntologyTerm> terms = new ArrayList<OntologyTerm>();

        JSONObject json = new JSONObject(result);
        JSONArray dataArray = json.getJSONArray("collection");
        for(int i=0;i<dataArray.length();i++) {
            JSONObject termJson = dataArray.getJSONObject(i);
            /*
            {
            prefLabel: "Melanoma"
            cui: [1]
                0:  "C0025202"
            -
            semanticType: [1]
                0:  "T191"
            -
            obsolete: false
            @id: "http://purl.bioontology.org/ontology/MEDDRA/10053571"
            @type: "http://www.w3.org/2002/07/owl#Class"
            links: {
                self: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571"
                ontology: "http://data.bioontology.org/ontologies/MEDDRA"
                children: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571/children"
                parents: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571/parents"
                descendants: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571/descendants"
                ancestors: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571/ancestors"
                tree: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571/tree"
                notes: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571/notes"
                mappings: "http://data.bioontology.org/ontologies/MEDDRA/classes/http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571/mappings"
                ui: "http://bioportal.bioontology.org/ontologies/MEDDRA?p=classes&conceptid=http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FMEDDRA%2F10053571"
            }
            }
            */
            JSONObject links = termJson.getJSONObject("links");

            OntologyTerm term = new OntologyTerm(links.getString("ontology"), termJson.getString("@id"), termJson.getString("prefLabel"));
            term.setChildUrl(links.getString("children"));
            term.setDescendantsUrl(links.getString("descendants"));
            terms.add(term);

        }

        return terms;
    }
}
