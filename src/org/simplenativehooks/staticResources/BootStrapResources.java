package org.simplenativehooks.staticResources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.simplenativehooks.utilities.FileUtility;

public class BootStrapResources {

	private static final Set<AbstractBootstrapResource> BOOTSTRAP_RESOURCES;

	private static final NativeHookBootstrapResources nativeHookResources;

	static {

		/*********************************************************************************/
		BOOTSTRAP_RESOURCES = new HashSet<>();

		nativeHookResources = new NativeHookBootstrapResources();
		BOOTSTRAP_RESOURCES.add(nativeHookResources);
	}

	public static void extractResources() throws IOException, URISyntaxException {
		for (AbstractBootstrapResource resource : BOOTSTRAP_RESOURCES) {
			resource.extractResources();
		}
	}

	protected static String getFile(String path) {
		return FileUtility.readFromStream(BootStrapResources.class.getResourceAsStream(path)).toString();
	}

	private BootStrapResources() {}
}
