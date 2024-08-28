package com.ericsson.eniq.events.metadataparser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Breaks up the UIMetaData file into its sub components.
 * Replaces WreckingBall, which is deprecated.
 * @author etonayr
 * @since 2011
 *
 */
public class Decompiler {

    private ObjectMapper mapper;

    public Decompiler() {
        mapper = new ObjectMapper();
    }

    private void writeFile(String name, JsonNode node, File parent) {
        File f = new File(parent, name + ".json");
        ObjectWriter writer = mapper.writer();
        writer = writer.withDefaultPrettyPrinter();
        try {
            ObjectNode n = mapper.createObjectNode();
            ArrayNode array = n.putArray(name);            
            array.addAll((ArrayNode) node);
            writer.writeValue(f, n);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFolderAndFile(String folder, JsonNode item, File out) {
        ObjectWriter writer = mapper.writer();
        writer = writer.withDefaultPrettyPrinter();
        ArrayNode node = (ArrayNode) item;

        Iterator<JsonNode> it = node.getElements();
        while (it.hasNext()) {
            JsonNode entry = it.next();

            JsonNode id = null;
            if (folder.equals("toolBars")) {
                id = entry.get("toolBarType");
            } else if (folder.equals("launchWindows")) {
                id = entry.get("launchWindowTypeId");
                if (id == null)
                    id = entry.get("id");
            } else {
                id = entry.get("id");
            }

            System.out.println(id.getValueAsText() + " " + folder);

            File dir = new File(out, folder);
            dir.mkdir();
            File f = new File(dir, id.getValueAsText() + ".json");

            try {
                ObjectNode output = mapper.createObjectNode();
                output.putAll((ObjectNode) entry);
                writer.writeValue(f, output);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void decompile(File file, File outDir) {
        try {
            JsonNode node = mapper.readValue(file, JsonNode.class);
            Iterator<Entry<String, JsonNode>> it = node.getFields();

            while (it.hasNext()) {
                Entry<String, JsonNode> entry = it.next();
                String key = entry.getKey();

                if (key.equals("grids") || key.equals("charts") || key.equals("chartDrillDownWindows")
                        || key.equals("drilldownWindows") || key.equals("launchWindows") || key.equals("toolBars")) {
                    writeFolderAndFile(key, entry.getValue(), outDir);
                } else {
                    writeFile(key, entry.getValue(), outDir);
                }
            }

        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("----------------------");
            System.out.println("usage: java -cp <jarfile> com.ericsson.eniq.events.metadataparser.Decompiler [json file] [output directory]");
            System.out.println("----------------------");
            System.exit(1);
        }
        
        File json = new File(args[0]);
        File outputDir = new File(args[1]);
        if(!outputDir.exists()) {
            outputDir.mkdir();
        }
        
        Decompiler decompiler = new Decompiler();
        decompiler.decompile(json, outputDir);
        //new File("C:/ccviews/eniq_events/eniq_events/eniq_events_services/services-main/src/main/resources/UIMetaData.json"
    }

}
