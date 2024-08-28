package com.ericsson.eniq.events.metadataparser;

import java.io.File;
import java.io.FilenameFilter;

public final class JsonFileFilter implements FilenameFilter {

	private final String fileExtension;
	
	public JsonFileFilter(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	@Override
	public boolean accept(File file, String arg1) {
		if(arg1.endsWith(fileExtension)) {
			return true;
		}
		return false;
	}

}
