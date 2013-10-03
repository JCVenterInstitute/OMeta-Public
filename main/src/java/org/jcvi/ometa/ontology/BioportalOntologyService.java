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
    private final String bioportalAddress = "http://rest.bioontology.org/bioportal/";
    private final String bioportalApiKey = "apikey=c6ae1b27-9f86-4e3c-9dcf-087e1156eabe";

    public BioportalOntologyService() {

    }

    public List<OntologyTerm> search(String search, String searchRootId, String ontologyId) throws Exception {
        String serviceUrl = this.buildUrl("search", search, searchRootId, ontologyId);
        System.out.println(serviceUrl);
        String resultString = this.callService(serviceUrl);

        List<OntologyTerm> results = null;
        if(resultString!=null && !resultString.isEmpty()) {
            results = this.processSearchResult(resultString);
        }

        return results;
    }

    private String buildUrl(String type, String search, String searchRootId, String ontologyId) {
        // /search/nephroblastoma/?maxnumhits=5&level=1&includeproperties=1&isexactmatch=0&ontologyids=null";
        // /virtual/ontology/1516?conceptid=http%3A%2F%2Fpurl.bioontology.org%2Fontology%2FICD10%2FO80-O84.9&light=0
        // /search/?query=tangle&ontologyids=1084&subtreerootconceptid=http%3A%2F%2Fontology.neuinfo.org%2FNIF%2FBiomaterialEntities%2FNIF-Subcellular.owl%23sao120573470

        final String responsePageParams = "&pagesize=all&pagenum=1&maxnumhits=1000";

        StringBuilder urlBuilder = new StringBuilder(bioportalAddress);
        if(type.equals("search")) {
            urlBuilder.append("search/?query=" + search);
        }

        if(searchRootId!=null && !searchRootId.isEmpty() && ontologyId!=null && !ontologyId.isEmpty()) {
            urlBuilder.append("&ontologyids=" + ontologyId);
            urlBuilder.append("&subtreerootconceptid=" + URLEncoder.encode(searchRootId));
            urlBuilder.append("&isexactmatch=0");
            urlBuilder.append("&includeproperties=1");
        }

        urlBuilder.append("&" + bioportalApiKey);
        return urlBuilder.toString();
    }

    private String callService(String url) throws Exception {
        String result = null;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept", "application/json");

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
        JSONArray dataArray = json.getJSONObject("success").getJSONArray("data");
        for(int i=0;i<dataArray.length();i++) {
            JSONObject data = dataArray.getJSONObject(i);
            JSONObject contents = data.getJSONObject("page").getJSONObject("contents");
            JSONArray resultArray = contents.getJSONObject("searchResultList").getJSONArray("searchBean");
            for(int j=0;j<resultArray.length();j++) {
                JSONObject termJson = resultArray.getJSONObject(j);
                /*
                    {
                        "ontologyVersionId":49697,
                        "ontologyId":1640,
                        "ontologyDisplayLabel":"Pediatric Terminology",
                        "recordType":"apreferredname",
                        "objectType":"class",
                        "conceptId":"http:\/\/www.owl-ontologies.com\/Ontology1358660052.owl#Nephroblastoma",
                        "conceptIdShort":"Nephroblastoma",
                        "preferredName":"Nephroblastoma",
                        "contents":"Nephroblastoma",
                        "isObsolete":0
                    }
                 */
                OntologyTerm term = new OntologyTerm(
                    termJson.getString("ontologyDisplayLabel"),
                    null,
                    termJson.getString("ontologyId"),
                    termJson.getString("conceptId"),
                    termJson.getString("contents")
                );
                term.setShortId(termJson.getString("conceptIdShort"));
                term.setOntologyVersion(termJson.getString("ontologyVersionId"));
                term.setPreferredName(termJson.getString("preferredName"));
                terms.add(term);
            }
            /*
            * list of ontologies found in a search result
            JSONArray hitOntologyArray = contents.getJSONObject("ontologyHitList").getJSONArray("ontologyHitBean");
            for(int j=0;j<hitOntologyArray.length();j++) {
                System.out.println(hitOntologyArray.getJSONObject(j));
            }
            */
        }

        return terms;
    }
}
