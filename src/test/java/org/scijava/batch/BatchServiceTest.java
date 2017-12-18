package org.scijava.batch;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Test;
import org.scijava.Context;

public class BatchServiceTest {
	
	private Context context;

	@After
	public void disposeContext() {
		if (context != null) {
			context.dispose();
			context = null;
		}
	}

	@Test
	public void testContext() {
		context = new Context(BatchService.class);
		final BatchService batchService =
			context.getService(BatchService.class);
		assertNotNull(batchService);
	}
}
