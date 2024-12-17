import org.json.simple.*;
import org.json.simple.parser.*;
import java.io.*;
import java.net.*;

public class E_PokeAPI {

    public static void main(String[] args) {
        new E_PokeAPI();
    }
    public E_PokeAPI() {
        System.out.println();
        JSONObject json = importJSON("https://pokeapi.co/api/v2/pokemon/charizard");
        System.out.println("Weight: "+json.get("weight"));
        System.out.println();
        System.out.println("Abilities:");
        JSONArray abilitiesArray = (JSONArray) json.get("abilities");
        for (Object o : abilitiesArray) {
            JSONObject abilityArrayItem = (JSONObject) o;
            JSONObject ability = (JSONObject) abilityArrayItem.get("ability");
            System.out.println(ability.get("name"));
        }
        System.out.println();
        System.out.println("Moves:");
        JSONArray movesArray = (JSONArray) json.get("moves");
        for (Object o : movesArray) {
            JSONObject movesArrayItem = (JSONObject) o;
            JSONObject move = ((JSONObject) movesArrayItem.get("move"));
            System.out.println((String) move.get("name"));
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

        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
