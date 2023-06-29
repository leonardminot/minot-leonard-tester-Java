package com.parkit.parkingsystem.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class JsonReaderUtil {
    private final static JSONParser jsonP = new JSONParser();
     public String getStringParameter(String jsonFile, String property) {
         String returnedProperty = "";
         try {
             JSONObject jsonO = (JSONObject) jsonP.parse(new FileReader(jsonFile));
             returnedProperty = (String) jsonO.get(property);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return returnedProperty;
     }
}
