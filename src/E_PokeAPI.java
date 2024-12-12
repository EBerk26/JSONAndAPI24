import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class E_PokeAPI {

    public static void main(String[] args) {
        new E_PokeAPI();
    }
    public E_PokeAPI() {
        JSONObject json = importJSON("https://pokeapi.co/api/v2/pokemon/charizard");
        System.out.println(json.get("weight"));

        System.out.println("Abilities:");
        JSONArray abilitiesArray = (JSONArray) json.get("abilities");
        for(int x =0;x<abilitiesArray.size();x++){
            JSONObject abilityArrayItem = (JSONObject) abilitiesArray.get(x);
            JSONObject ability = (JSONObject) abilityArrayItem.get("ability");
            System.out.println(ability.get("name"));
        }
        System.out.println();
        System.out.println("Moves:");
        JSONArray movesArray = (JSONArray) json.get("moves");
        for(int x =0;x<movesArray.size();x++){
            JSONObject movesArrayItem = (JSONObject) movesArray.get(x);
            JSONObject move = ((JSONObject) movesArrayItem.get("move"));
            System.out.println((String)move.get("name"));
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
