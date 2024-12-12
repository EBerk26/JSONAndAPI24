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

public class D_ReadATLA {

    public static void main(String[] args) throws ParseException {
        D_ReadATLA atla = new D_ReadATLA();
    }

    public D_ReadATLA() throws ParseException {
        pull();
    }

    public void pull() throws ParseException {
        String output = "";
        String jsonString="";
        try {

            URL url = new URL("https://last-airbender-api.fly.dev/api/v1/characters"); /** Your API's URL goes here */
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {

                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));


            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                jsonString += output;
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // turn your string into a JSON object using a parser
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(jsonString);

        JSONObject firstCharacter = (JSONObject) jsonArray.getFirst();
        JSONArray firstCharacterAllies = (JSONArray) firstCharacter.get("allies");
        for(int x =0;x<firstCharacterAllies.size();x++){
            System.out.println(firstCharacterAllies.get(x));
        }

        System.out.println();

        for(int x = 0;x<=19;x++) {
            JSONObject character = (JSONObject) jsonArray.get(x); // 0 index is the first character
            System.out.println(character.get("name"));
        }

    }

}