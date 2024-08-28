package com.ericsson.eniq.events.metadataparser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MergeControllerTest {

    private File out1;

    private File out2;

    private File testMerge;

    @Before
    public void setup() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array1 = mapper.createArrayNode();

        ObjectNode node1 = mapper.createObjectNode();
        node1.put("length", "100px");
        node1.put("width", "200px");
        node1.put("comment", "blah");
        array1.add(node1);

        ObjectNode parent1 = mapper.createObjectNode();
        parent1.put("parent1", array1);

        ArrayNode array2 = mapper.createArrayNode();
        ObjectNode node2 = mapper.createObjectNode();
        node2.put("length", "500px");
        node2.put("comment", "This is a comment");
        node2.put("width", "500px");
        array2.add(node2);

        ObjectNode parent2 = mapper.createObjectNode();
        parent2.put("parent2", array2);

        out1 = File.createTempFile("node1", ".json");
        out2 = File.createTempFile("node2", ".json");
        //Using txt extension so merge controller won't pick up an empty file from test dir.
        testMerge = File.createTempFile("testMerge", ".txt");

        mapper.writeValue(out1, parent1);
        mapper.writeValue(out2, parent2);
    }

    @Test    
    public void testMerge() throws Exception {
        Log logger = mock(Log.class);
        String baseDir = System.getProperty("java.io.tmpdir");
        MergeController controller = new MergeController(baseDir, ".json", testMerge.getAbsolutePath(), logger);
        controller.merge();

        assertTrue(testMerge.exists());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tree = mapper.readTree(new FileInputStream(testMerge));

        JsonNode node1 = tree.get("parent1");
        JsonNode node2 = tree.get("parent2");
        
        assertNotNull(node1);
        assertNotNull(node2); 
        ArrayNode arrayNode1 = (ArrayNode)node1;
        JsonNode testCommentRemoved1 = arrayNode1.get(0);
        assertTrue(testCommentRemoved1.path("comment").isMissingNode());           
        
        ArrayNode arrayNode2 = (ArrayNode)node2;
        JsonNode testCommentRemoved2 = arrayNode2.get(0);
        assertTrue(testCommentRemoved2.path("comment").isMissingNode());
    }

    @Test
    @Ignore
    public void realTest() throws Exception {
        List<String> bases = new ArrayList<String>();
        bases.add("C:/ccviews/eniq_events/eniq_events/eniq_events_services/services-main/src/main/resources/uimetadata/mss");
        bases.add("C:/ccviews/eniq_events/eniq_events/eniq_events_services/services-main/src/main/resources/uimetadata/standard");

        List<String> outputs = new ArrayList<String>();
        outputs.add("C:/ccviews/eniq_events/eniq_events/eniq_events_services/services-main/src/main/resources/UIMetaData.json.txt");
        outputs.add("C:/ccviews/eniq_events/eniq_events/eniq_events_services/services-main/src/main/resources/UIMetaData_MSS.json.txt");

        for (int i = 0; i < bases.size(); i++) {
            new MergeController(bases.get(i), ".json", outputs.get(i), new MetaDataCompiler().getLog()).merge();            
        }
    }

    @After
    public void cleanup() {
        out1.delete();
        out2.delete();
        testMerge.delete();
    }
}