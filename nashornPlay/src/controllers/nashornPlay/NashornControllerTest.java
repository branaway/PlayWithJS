package controllers.nashornPlay;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class NashornControllerTest {
	@Test
	public void testLoadingPlayHeaders() throws IOException {
		InputStream resourceAsStream = this.getClass().getResourceAsStream(NashornController.PLAY_HEADERS_JS);
		int available = resourceAsStream.available();
		assertTrue(available > 0);
	}

}
