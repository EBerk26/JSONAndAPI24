import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// video to load jar
//https://www.youtube.com/watch?v=QAJ09o3Xl_0 (from about 37 seconds)

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class C_ReadSWAPI {

    public static void main(String[] args) throws ParseException {
        new C_ReadSWAPI();
    }

    public C_ReadSWAPI() throws ParseException {
        pull();
    }

    public void pull() throws ParseException {


        // turn your string into a JSON object using a parser

        JSONObject json = importJSON("https://swapi.dev/api/people/4/");
        // get a single value out of the json
        String height = (String) json.get("height");
        System.out.println("HEIGHT: " + height);

        // get a json array out of the json
        printArrayList(json,"films");

        System.out.println("Name: "+json.get("name"));

        JSONObject homeworld = importJSON((String)(json.get("homeworld")));
        System.out.println("Homeworld: "+homeworld.get("name"));
        System.out.println("Climate: "+homeworld.get("climate"));

    }
    void printArrayList(JSONObject jsonObject, String key){ //get and print an array from the JSON
        if(jsonObject.containsKey(key)){
            JSONArray jsonArray = (JSONArray) jsonObject.get(key);
            if(!jsonArray.isEmpty()){
                System.out.println(key.toUpperCase()+":");
                for (Object o : jsonArray) {
                    String object = (String) o;
                    System.out.println(object);
                }
            }
        }
    }

    JSONObject importJSON(String link){
        String output;
        StringBuilder jsonString= new StringBuilder();
        try {

            URL url = new URL(link); // Your API's URL goes here
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {

                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));


            while ((output = br.readLine()) != null) {
                jsonString.append(output);
            }

            conn.disconnect();
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(jsonString.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
        catch (ParseException e){
            e.printStackTrace();
            return null;

        }
    }

}

