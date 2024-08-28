package com.ericsson.eniq.events.metadataparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * 
 * <h1>MetaDataCompiler Maven Plugin</h1>
 * 
 * <p>Watch out for the Maven annotations in the doc comments in this class. 
 * Don't remove them as they are needed for this to work, see the <a href="http://maven.apache.org/guides/plugin/guide-java-plugin-development.html">Guide to Developing Maven Plugins</a> for details </p> 
 * 
 * @author etonayr
 * @goal compile-meta-data
 * 
 */
public class MetaDataCompiler extends AbstractMojo {

    /**
     * The full path to the directory in which the json component files are
     * stored.
     * 
     * @parameter
     */
    private List<String> baseDirectory = new ArrayList<String>();

    /**
     * Specify the file extension of the component Json files, default is .json.
     * 
     * @parameter default-value=".json"
     */
    private String fileInclusionExtension;

    /**
     * The name of the output file which will contain the compiled meta data
     * 
     * @parameter
     */
    private List<String> outputFile = new ArrayList<String>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateDirectoryParams();

        try {
            for (int i = 0; i < baseDirectory.size(); i++) {
                new MergeController(baseDirectory.get(i), fileInclusionExtension, outputFile.get(i), getLog()).merge();
            }
        } catch (final JsonParseException e) {
            final String error = "Invalid JSON was detected details " + e.getMessage();
            throw new MojoExecutionException(error);
        } catch (final JsonMappingException e) {
            getLog().error(e);
        } catch (final IOException e) {
            getLog().error(e);
        }
    }

    /*
     * Check to make sure all the directories specified in the pom exist.
     */
    private void validateDirectoryParams() throws MojoExecutionException {
        for (final String path : baseDirectory) {
            final File base = new File(path);
            if (!base.isDirectory() && !base.exists()) {
                getLog().error(new MojoFailureException("The base directory " + path + " could not be found."));
                throw new MojoExecutionException("The base directory " + path + " could not be found.");
            }
        }
    }

    public void setBaseDirectory(final List<String> baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void setFileInclusionExtension(final String fileInclusionExtension) {
        this.fileInclusionExtension = fileInclusionExtension;
    }

    public void setOutputFile(final List<String> outputFile) {
        this.outputFile = outputFile;
    }
}
