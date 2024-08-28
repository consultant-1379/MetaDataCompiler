package com.ericsson.eniq.events.metadataparser;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Validator {
    
    
    /**
     * Rough n' Ready JSON validator
     * @param args
     */
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("No file specified");
        }
        
        File jsonFile = new File(args[0]);
        if(!jsonFile.exists()){
            System.out.println("Could not find file "+jsonFile);
            System.exit(1);
        }        
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(jsonFile, JsonNode.class);
            System.out.println("No validation errors found");
        } catch (JsonParseException e) {
            System.out.println("JSON error at line: "+e.getLocation().getLineNr());
            System.out.println("Details: "+e.getMessage());
        } catch (JsonMappingException e) {
            System.out.println("JSON error at line: "+e.getLocation().getLineNr());
            System.out.println("Details: "+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

}
