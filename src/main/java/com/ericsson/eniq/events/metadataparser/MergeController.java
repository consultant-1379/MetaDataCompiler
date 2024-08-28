package com.ericsson.eniq.events.metadataparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

/**
 * Merges multiple JSON files into a single JSON file.
 * 
 * @author etonayr
 * @since 2011
 *
 */
public final class MergeController {

    private final Log logger;

    private final String baseDirectory;

    private String fileExtension;

    private final String outputFile;

    private final ObjectMapper mapper;

    /**
     * Constructor, baby.
     * 
     * @param baseDir           The directory which contains the input files
     * @param fileExtension     The extension of the input files to collect
     * @param outputFile        The name of the output file to write to
     * @param logger            Maven Mojo logger reference, needed for runtime console output
     */
    public MergeController(final String baseDir, final String fileExtension, final String outputFile, final Log logger) {
        this.baseDirectory = baseDir;
        this.fileExtension = fileExtension;
        this.outputFile = outputFile;
        mapper = new ObjectMapper();
        this.logger = logger;
    }

    /**
     * Collect the files, bake for 20 cycles and serve.
     * 
     * @throws JsonParseException       You probably have some invalid JSON in one of your input files
     * @throws JsonMappingException     Trip to the Jackson docs if your getting this one.
     * @throws IOException              Check your files exist.
     */
    public void merge() throws JsonParseException, JsonMappingException, IOException {
        final List<ObjectNode> data = collectMetaDataComponentObjects();
        final ObjectWriter ow = mapper.prettyPrintingWriter(new DefaultPrettyPrinter());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(outputFile));
            logger.info("Writing new combined UI meta data file to " + outputFile);
            ow.writeValue(fos, createMetaDataObject(data));
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
        logger.info("Finished.");
    }

    private ObjectNode collectMetaDataComponentObjects(final File directory) throws JsonParseException,
            JsonMappingException, IOException {
        logger.info("Collecting JSON files from " + directory);
        final List<JsonNode> data = new ArrayList<JsonNode>();
        final ObjectNode parent = mapper.createObjectNode();
        final ArrayNode node = mapper.createArrayNode();

        final File[] files = directory.listFiles(new JsonFileFilter(fileExtension));
        logger.info(files.length + " JSON files found");
        for (final File file : files) {
            final ObjectNode tNode = mapper.readValue(file, ObjectNode.class);
            removeCommentsFromNode(tNode);
            data.add(tNode);
        }
        node.addAll(data);
        parent.put(directory.getName(), node);
        return parent;
    }

    /*
     * Collects all the meta data files from the input directory and converts them to JSON objects.
     */
    private List<ObjectNode> collectMetaDataComponentObjects() throws JsonParseException, JsonMappingException,
            IOException {
        logger.info("Collecting JSON files from " + baseDirectory);
        final List<ObjectNode> data = new ArrayList<ObjectNode>();

        final File[] files = new File(baseDirectory).listFiles(new JsonFileFilter(fileExtension));
        logger.info(files.length + " JSON files found");
        for (final File file : files) {
            final ObjectNode target = mapper.readValue(file, ObjectNode.class);
            final Iterator<JsonNode> it = target.getElements();
            while (it.hasNext()) {
                final JsonNode result = it.next();
                if (result instanceof ArrayNode) {
                    removeCommentsFromArrayNodeObjects((ArrayNode) result);
                } else {
                    removeCommentsFromNode((ObjectNode) result);
                }
            }
            data.add(target);
        }

        data.addAll(getMetaDataFromSubDirectories());

        return data;
    }

    /*
     * Creates a big JSON object from all the collected files
     */
    private ObjectNode createMetaDataObject(final List<ObjectNode> data) {
        logger.info("Merging JSON objects");
        final ObjectNode newNode = mapper.createObjectNode();
        for (final ObjectNode jsonNode : data) {
            newNode.putAll(jsonNode);
        }
        return newNode;
    }

    private List<ObjectNode> getMetaDataFromSubDirectories() throws JsonParseException, JsonMappingException,
            IOException {

        final File[] files = new File(baseDirectory).listFiles();
        final List<ObjectNode> list = new ArrayList<ObjectNode>(files.length);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                list.add(collectMetaDataComponentObjects(files[i]));
            }
        }

        return list;
    }

    private void removeCommentsFromArrayNodeObjects(final ArrayNode node) {
        for (int i = 0; i < node.size(); i++) {
            removeCommentsFromNode((ObjectNode) node.get(i));
        }
    }

    private void removeCommentsFromNode(final ObjectNode node) {
        if (!node.findPath("comment").isMissingNode()) {
            node.remove("comment");
        }
    }

    /**
     * Decides what files to collect by the file extension.
     * @param extension
     */
    public void setFileInclusionExtension(final String extension) {
        this.fileExtension = extension;
    }
}
