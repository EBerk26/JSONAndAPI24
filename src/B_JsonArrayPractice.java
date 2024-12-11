import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class B_JsonArrayPractice {

    public static void main(String[] args) throws ParseException {
        new B_JsonArrayPractice();
    }

    public B_JsonArrayPractice() throws ParseException {
        String s = "{\"name\":\"Darth Vader\",\"height\":\"202\",\"mass\":\"136\",\"hair_color\":\"none\",\"skin_color\":\"white\",\"eye_color\":\"yellow\",\"birth_year\":\"41.9BBY\",\"gender\":\"male\",\"homeworld\":\"https://swapi.dev/api/planets/1/\",\"films\":[\"https://swapi.dev/api/films/1/\",\"https://swapi.dev/api/films/2/\",\"https://swapi.dev/api/films/3/\",\"https://swapi.dev/api/films/6/\"],\"species\":[],\"vehicles\":[],\"starships\":[\"https://swapi.dev/api/starships/13/\"],\"created\":\"2014-12-10T15:18:20.704000Z\",\"edited\":\"2014-12-20T21:17:50.313000Z\",\"url\":\"https://swapi.dev/api/people/4/\"}\n";
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) (parser.parse(s));

        System.out.println("String format: " + s);
        System.out.println("JSON format: " + json);

        // get a single value out of the json
        String height = (String) json.get("height");
        System.out.println("HEIGHT: " + height);

        // get a json array out of the json
        printArrayList(json,"films");
        printArrayList(json,"starships");
    } // end of constructor


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
}
