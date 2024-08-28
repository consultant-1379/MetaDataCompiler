package com.ericsson.eniq.events.metadataparser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 * Breaks up the UIMetaData file into its sub components. <br>
 * Superseded by Decompiler
 * 
 * @deprecated 
 * @see {@link Decompiler}
 * @author etonayr
 * @since 2011
 *
 */
@Deprecated
public class WreckingBall {

    private WreckingBall() {

    }

    public void breakUp(File metaFile, File outputDir) throws JsonGenerationException, JsonMappingException,
            IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.defaultPrettyPrintingWriter();

        JsonNode uimeta = mapper.readValue(metaFile, JsonNode.class);
        Iterator<Entry<String, JsonNode>> fields = uimeta.getFields();
        while (fields.hasNext()) {
            Entry<String, JsonNode> field = fields.next();
            ObjectNode newNode = mapper.createObjectNode();            
            newNode.put(field.getKey(), field.getValue());
            File componentfile = new File(outputDir.getAbsolutePath() + System.getProperty("file.separator")
                    + field.getKey() + ".json");
            writer.writeValue(componentfile, newNode);
        }
    }

    /**
     *
     * 
     * @param args
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
        if (args.length != 2) {
            System.out.println("Arguments not specified");
            System.out.println("Usage: WreckingBall [Meta data file] [Output directory]");
            System.exit(1);
        }

        File meta = new File(args[0]);
        File outDir = new File(args[1]);

        if (!meta.exists()) {
            System.out.println("Meta data file not found");
            System.exit(1);
        }

        if (!outDir.exists()) {
            System.out.println("Output directory could not be found");
            System.exit(1);
        }

        WreckingBall util = new WreckingBall();
        util.breakUp(meta, outDir);
    }

}
