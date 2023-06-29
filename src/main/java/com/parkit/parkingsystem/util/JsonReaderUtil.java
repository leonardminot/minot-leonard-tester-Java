package com.parkit.parkingsystem.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class JsonReaderUtil {
    private final static JSONParser jsonP = new JSONParser();
     public String getStringParameter(String jsonFile, String property) {
         String returnedProperty = "";
         try (FileReader fileReader = new FileReader(jsonFile, StandardCharsets.UTF_8)){
             JSONObject jsonO = (JSONObject) jsonP.parse(fileReader);
             returnedProperty = (String) jsonO.get(property);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return returnedProperty;
     }
}
