package rp.folkevognen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

class Settings {
    public String token;
    public Map<String, Integer> folkevognen;
    public int lastFolkedWeek;
    public int lastFolkedYear;
    public String lastFolker;

    public Settings() {
        String path = "settings.json";
        File file = new File(path);
        if (!file.exists()) {
            try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write("{\n}");
            writer.close();
            } catch (IOException e) {
            e.printStackTrace();
            }
        }
        // {"token": "your_token_here", "folkevognen": [{"name": "name", "count": 1}]}
        String tokenVal = "";
        int lastFolkedWeekVal = 0;
        int lastFolkedYearVal = 0;
        String lastFolkerVal = "";
        Map<String, Integer> folkevognenVal = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(new String(Files.readAllBytes(Paths.get(file.toURI()))));
            tokenVal = obj.getString("token");
            lastFolkedWeekVal = obj.getInt("lastFolkedWeek");
            lastFolkedYearVal = obj.getInt("lastFolkedYear");
            lastFolkerVal = obj.getString("lastFolker");
            JSONObject folkevognenObj = obj.getJSONObject("folkevognen");
            for(int i = 0; i < folkevognenObj.length(); i++) {
                folkevognenVal.put(folkevognenObj.names().getString(i), folkevognenObj.getInt(folkevognenObj.names().getString(i)));
            }
        } catch(IOException | JSONException e) {
            e.printStackTrace();
            System.exit(1);
        }
        token = tokenVal;
        lastFolkedWeek = lastFolkedWeekVal;
        lastFolkedYear = lastFolkedYearVal;
        lastFolker = lastFolkerVal;
        folkevognen = folkevognenVal;
    }

    public void write() {
        String path = "settings.json";
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("{\n}");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            JSONObject obj = new JSONObject(new String(Files.readAllBytes(Paths.get(file.toURI()))));
            obj.put("lastFolkedWeek", lastFolkedWeek);
            obj.put("lastFolkedYear", lastFolkedYear);
            obj.put("lastFolker", lastFolker);
            JSONObject folkevognenObj = new JSONObject();
            for (String key : folkevognen.keySet()) {
            folkevognenObj.put(key, folkevognen.get(key));
            }
            obj.put("folkevognen", folkevognenObj);
            FileWriter writer = new FileWriter(file);
            writer.write(obj.toString());
            writer.close();
        } catch(IOException | JSONException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void write(Map<String, Integer> folkevognen, String lastFolker) {
        String path = "settings.json";
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("{\n}");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            JSONObject obj = new JSONObject(new String(Files.readAllBytes(Paths.get(file.toURI()))));
            obj.put("lastFolkedWeek", Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
            obj.put("lastFolkedYear", Calendar.getInstance().get(Calendar.YEAR));
            obj.put("lastFolker", lastFolker);
            JSONObject folkevognenObj = new JSONObject();
            for (String key : folkevognen.keySet()) {
                folkevognenObj.put(key, folkevognen.get(key));
            }
            obj.put("folkevognen", folkevognenObj);
            FileWriter writer = new FileWriter(file);
            writer.write(obj.toString());
            writer.close();
        } catch(IOException | JSONException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
