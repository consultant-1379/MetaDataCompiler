package com.ericsson.eniq.events.metadataparser;

import static org.junit.Assert.*;

import org.junit.Test;

public class JsonFileFilterTest {

	@Test
	public void testJsonFileFilter() {
		assertNotNull(new JsonFileFilter(".json"));
	}

	@Test
	public void testAccept() {
		JsonFileFilter filter = new JsonFileFilter(".json");
		assertTrue(filter.accept(null, "MyFile.json"));
		assertFalse(filter.accept(null, "MyFile.blah"));
	}
}
