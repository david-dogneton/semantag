/*
 * Copyright 2012 DBpedia Spotlight Development Team
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Check our project website for information on how to acknowledge the authors and how to contribute to the project: http://spotlight.dbpedia.org
 */

package controllers.sparql;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Gets a list of DBpediaResources matching a given SPARQL query.
 * Will be used for filtering annotations.
 *
 * @author PabloMendes
 *
 */
public class SparqlQueryExecuter {

    // Create an instance of HttpClient.
    private static HttpClient client = new HttpClient();

    String mainGraph;
    String sparqlUrl;

    public SparqlQueryExecuter(String mainGraph, String sparqlUrl) {
        this.mainGraph = mainGraph;
        this.sparqlUrl = sparqlUrl;
    }

    // this is the virtuoso way. subclasses can override for other implementations
    //http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query=select+distinct+%3Fpol+where+{%3Fpol+a+%3Chttp://dbpedia.org/ontology/Politician%3E+}&debug=on&timeout=&format=text/html&save=display&fname=
    protected URL getUrl(String query) throws UnsupportedEncodingException, MalformedURLException {
        String graphEncoded = URLEncoder.encode(mainGraph, "UTF-8");
        String formatEncoded = URLEncoder.encode("application/sparql-results+json", "UTF-8");
        String queryEncoded = URLEncoder.encode(query, "UTF-8");
        String url = sparqlUrl+"?"+"default-graph-uri="+graphEncoded+"&query="+queryEncoded+"&format="+formatEncoded+"&debug=on&timeout=";
        return new URL(url);
    }

    public JSONArray query(String query) throws IOException, OutputException, SparqlExecutionException {
        if (query==null) return new JSONArray();
        JSONArray uris = new JSONArray();
		/*LOG.debug("--SPARQL QUERY: " + query.replace("\n", " "));
*/
        URL url = getUrl(query);
        //LOG.trace(url);

        String response = null;
        try {
//            uris = parse(readOutput(get(url)));
            response = request(url);
            uris = parse(response);
        } catch (JSONException e) {
            throw new OutputException(e+response);
        }
/*        LOG.debug(String.format("-- %s found.", uris.size()));*/
        return uris;
    }

    public String update(String query) throws SparqlExecutionException {
        String response = null;
        try {
            URL url = getUrl(query);
            response = request(url);
        } catch (Exception e) {
            throw new SparqlExecutionException(e);
        }
        return response;
    }

    public String request(URL url) throws SparqlExecutionException {
        GetMethod method = new GetMethod(url.toString());
        String response = null;

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("SparqlQuery failed: " + method.getStatusLine());
                throw new SparqlExecutionException(String.format("%s (%s). %s",
                        method.getStatusLine(),
                        method.getURI(),
                        method.getResponseBodyAsString()));
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            response = new String(responseBody);

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            throw new SparqlExecutionException(e);
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            throw new SparqlExecutionException(e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return response;

    }

/*    *//**
     * Parses SPARQL+JSON output, getting a list of DBpedia URIs returned in *any* variable in the query.
     * Consider moving to a class on its own if we ever use this anywhere else in the code.
     *
     * @paramjsonString string representation of SPARQL+JSON results
     * @return list of URIs as Strings contained in any variables in this result.
     * @throws org.json.JSONException
     */
    private static JSONArray parse(String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);
        JSONArray vars = root.getJSONObject("head").getJSONArray("vars");
        JSONArray bindings = root.getJSONObject("results").getJSONArray("bindings");

/*        for (int i = 0; i< bindings.length(); i++) {
            JSONObject row = bindings.getJSONObject(i);
            for (int v = 0; v < vars.length(); v++) {
                JSONObject typeValue = row.getJSONObject((String) vars.get(v));
                String uri = typeValue.getString("value").replace(SpotlightConfiguration.DEFAULT_NAMESPACE, "");
                results.add(uri);
            }
        }*/

        return bindings;
    }

    public static String getImageFromView(String uri){
        String img ="noPic";
        String requete = "select ?thumb {<"+uri+"> dbpedia-owl:thumbnail ?thumb}";
        JSONArray uris = null;
        SparqlQueryExecuter sparql = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql");
        try {
            uris = sparql.query(requete);
            if(uris!=null && uris.length()>0){
                try {
                    img = uris.getJSONObject(0).getJSONObject("thumb").getString("value");
                } catch (JSONException e) {
                    //pas d'image
                }
            };
        } catch (Exception e) {
            //pas d'image
        }
        return img;
    }

    public String getImage(String uri){
        String img ="noPic";
        String requete = "select ?thumb {<"+uri+"> dbpedia-owl:thumbnail ?thumb}";
        JSONArray uris = null;
        try {
            uris = this.query(requete);
            if(uris!=null && uris.length()>0){
                try {
                    JSONObject res = uris.getJSONObject(0).getJSONObject("thumb");
                    String tmp = res.getString("value");
                    if(!tmp.contains("DOCTYPE")) {
                        img = res.getString("value");
                    }
                } catch (JSONException e) {
                    img ="noPic";
                }
            };
        } catch (Exception e) {
            img ="noPic";
        }
        return img;
    }

    public String getImageFullSize(String uri){
        String img ="noPic";
        String requete = "select ?thumb {<"+uri+"> foaf:depiction ?thumb}";
        JSONArray uris = null;
        try {
            uris = this.query(requete);
            if(uris!=null && uris.length()>0){
                try {
                    JSONObject res = uris.getJSONObject(0).getJSONObject("thumb");
                    String tmp = res.getString("value");
                    if(!tmp.contains("DOCTYPE")) {
                        img = res.getString("value");
                    }
                } catch (JSONException e) {
                    img ="noPic";
                }
            };
        } catch (Exception e) {
            img ="noPic";
        }
        return img;
    }

    public String getImageDescription(String uri) throws Exception {
        String desc ="";
        String requete = "select ?thumbCaption {<"+uri+"> dbpedia-owl:thumbnailCaption ?thumbCaption}";
        JSONArray uris = this.query(requete);
        if(uris!=null && uris.length()>0){
            JSONObject thumbCaption = uris.getJSONObject(0).getJSONObject("thumbCaption");
            if(thumbCaption!=null) {
                String tmp = thumbCaption.getString("value");
                if (!tmp.contains("DOCTYPE")){
                    desc = tmp;
                }
            }
        };
        return desc;
    }

    public String getAbstract(String uri) throws Exception {
        String img = null;
        String requete = "select ?abstract {<"+uri+"> dbpedia-owl:abstract ?abstract}";
        JSONArray uris = this.query(requete);
        if(uris!=null && uris.length()>0){
            img = uris.getJSONObject(0).getJSONObject("abstract").getString("value");
        }
        return img;
    }

    public String getWikiLink(String uri) throws Exception {
        String img ="";
        String requete = "select ?topic {<"+uri+"> foaf:isPrimaryTopicOf ?topic}";

        JSONArray uris = this.query(requete);
        if(uris!=null && uris.length()>0){
            img = uris.getJSONObject(0).getJSONObject("topic").getString("value").replace("en.","fr.");
        };
        return img;
    }

    public String getName(String uri){
        String name ="";
        String requete = "select ?name {<"+uri+"> rdfs:label ?name}";
        JSONArray uris = null;
        try {
            uris = this.query(requete);
            if(uris!=null && uris.length()>0){
                JSONObject res = null;
                res = uris.getJSONObject(0).getJSONObject("name");
                String tmp = res.getString("value");
                if(!tmp.contains("DOCTYPE")) {
                    name = res.getString("value");
                }
            }else{
                name=this.getTitle(uri);
            }
        } catch (Exception e) {
            name=this.getTitle(uri);
        }
        return name;
    }

    public static String getNameFromView(String uri){
        String name ="";
        String requete = "select ?name {<"+uri+"> rdfs:label ?name}";
        SparqlQueryExecuter sparql = new SparqlQueryExecuter("http://fr.dbpedia.org", "http://fr.dbpedia.org/sparql");
        JSONArray uris = null;
        try {
            uris = sparql.query(requete);
            if(uris!=null && uris.length()>0){
                JSONObject res = null;
                res = uris.getJSONObject(0).getJSONObject("name");
                String tmp = res.getString("value");
                if(!tmp.contains("DOCTYPE")) {
                    name = res.getString("value");
                }
            }else{
                name=sparql.getTitle(uri);
            }
        } catch (Exception e) {
            name=sparql.getTitle(uri);
        }
        return name;
    }

    public String getTitle(String uri){
        String title ="";
        String requete = "select ?name {<"+uri+"> dbpedia-owl:title ?name}";
        JSONArray uris = null;
        try {
            uris = this.query(requete);
        } catch (Exception e) {
            //pas de title
        }
        if(uris!=null && uris.length()>0){
            try {
                JSONObject res = uris.getJSONObject(0).getJSONObject("name");
                String tmp = res.getString("value");
                if(!tmp.contains("DOCTYPE")) {
                    title = res.getString("value");
                }
            } catch (Exception e) {
                //pas de title
            }
        }

        return title;
    }


    public static void main(String[] args) throws Exception {

        String example = "SELECT ?resource ?label ?score WHERE {\n" +
                "?resource ?relation <http://dbpedia.org/resource/India> .\n" +
                "GRAPH ?g {\n" +
                "?resource <http://www.w3.org/2004/02/skos/core#altLabel> ?label.\n" +
                "}\n" +
                "?g  <http://dbpedia.org/spotlight/score>  ?score.\n" +
                "FILTER (REGEX(?label, \"woolworth\", \"i\"))\n" +
                "}";

        String example2 = "select distinct ?act where {?act a <http://dbpedia.org/ontology/Actor>}";

        // String url = "http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query=select+distinct+%3Fpol+where+{%3Fpol+a+%3Chttp://dbpedia.org/ontology/Politician%3E+}&debug=on&timeout=&format=text/html&save=display&fname=";
        SparqlQueryExecuter e = new SparqlQueryExecuter("http://dbpedia.org", "http://dbpedia.org/sparql");
        JSONArray uris = e.query(example2);
        System.out.println(uris);
    }

}
