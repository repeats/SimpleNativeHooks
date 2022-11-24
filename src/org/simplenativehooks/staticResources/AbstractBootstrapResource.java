package org.simplenativehooks.staticResources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.simplenativehooks.utilities.FileUtility;
import org.simplenativehooks.utilities.Function;

public abstract class AbstractBootstrapResource {

	private static final Logger LOGGER = Logger.getLogger(AbstractBootstrapResource.class.getName());

	protected void extractResources() throws IOException, URISyntaxException {
		if (!FileUtility.createDirectory(getExtractingDest().getAbsolutePath())) {
			LOGGER.warning("Failed to extract " + getName() + " resources");
			return;
		}

		final String path = getRelativeSourcePath();
		FileUtility.extractFromCurrentJar(path, getExtractingDest(), new Function<String, Boolean>() {
			@Override
			public Boolean apply(String name) {
				return correctExtension(name);
			}
		}, new Function<String, Boolean>() {
			@Override
			public Boolean apply(String name) {
				return postProcessing(name);
			}
		});
	}

	protected boolean postProcessing(String name) {
		return true;
	}
	protected abstract boolean correctExtension(String name);
	protected abstract String getRelativeSourcePath();
	protected abstract File getExtractingDest();
	protected abstract String getName();
}
