package controllers;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import play.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.LinkedList;
import java.util.List;

public class Annotator {
    private static final String production = "http://spotlight.dbpedia.org/rest/annotate";
    private static final String fr = "http://spotlight.sztaki.hu:2225/rest/annotate";

/*    private static String shortText = "Agathe Habyarimana, 69, is accused by the Rwandan authorities " +
            "of helping to plan the genocide. She denies the accusations. French forces flew her out of Rwanda " +
            "shortly after the violence began and she has lived in France for years. More than 800,000 Tutsis ";*/

    public static List<List<String>> annotate(String shortText) throws JSONException {
        List<List<String>> entrees=new LinkedList<>();
        String json1 = jersey_client(shortText, fr, "Document");
//        Logger.debug("JSON : " + json1);
        if(json1.length()>0){
        final JSONObject obj = new JSONObject(json1);
        final JSONArray resources = obj.getJSONArray("Resources");
        final int n = resources.length();
        for (int i = 0; i < n; ++i) {
            final JSONObject resource = resources.getJSONObject(i);
            List<String> donnees=new LinkedList<>();
            donnees.add(resource.getString("@surfaceForm"));
            donnees.add(resource.getString("@URI"));
            donnees.add(resource.getString("@types"));
            entrees.add(donnees);
        }
        }
        else{
            List empty = new LinkedList<String>();
            empty.add("Pas de résultats.");
            entrees.add(empty);
        }
        return entrees;
    }



    private static String jersey_client(String text, String url, String disambiguator) {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        String json = "";
        try {
            MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
            /*queryParams.add("disambiguator", disambiguator); */
            queryParams.add("confidence", "0.5");
            queryParams.add("support", "-1");
            queryParams.add("text", text);
            //queryParams.add("text", java.net.URLEncoder.encode(text,"UTF-8"));


            json = webResource.
                    accept( MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).
                    header("ContentType","application/x-www-form-urlencoded;charset=UTF-8").
                            post(String.class, queryParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.concat("\n");
    }
}
