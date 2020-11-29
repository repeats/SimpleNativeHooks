package org.simplenativehooks;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.simplenativehooks.linux.GlobalLinuxEventOchestrator;
import org.simplenativehooks.osx.GlobalOSXEventOchestrator;
import org.simplenativehooks.utilities.OSIdentifier;
import org.simplenativehooks.windows.GlobalWindowsEventOchestrator;
import org.simplenativehooks.windows.GlobalWindowsJnaEventOchestrator;
import org.simplenativehooks.x11.GlobalX11EventOchestrator;

public class NativeHookInitializer {

	private static final Logger LOGGER = Logger.getLogger(NativeHookInitializer.class.getName());
	public static final String VERSION = "0.0.4";
	public static final boolean USE_X11_ON_LINUX = true;

	private final Config config;

	private NativeHookInitializer(Config config) {
		this.config = config;
	}

	public static NativeHookInitializer of() {
		return new NativeHookInitializer(Config.Builder.of().useJnaForWindows(true).build());
	}

	public static NativeHookInitializer of(Config config) {
		return new NativeHookInitializer(config);
	}

	public static class Config {
		private boolean useJnaForWindows;

		private Config(boolean useJnaForWindows) {
			this.useJnaForWindows = useJnaForWindows;
		}

		public static class Builder {
			private boolean useJnaForWindows;

			private Builder() {}

			public static Builder of() {
				return new Builder();
			}

			/**
			 * If JNA is used for Windows, there is no need to call resource extraction.
			 */
			public Builder useJnaForWindows(boolean useJnaForWindows) {
				this.useJnaForWindows = useJnaForWindows;
				return this;
			}

			public Config build() {
				return new Config(useJnaForWindows);
			}
		}
	}

	public void start() {
		if (OSIdentifier.IS_WINDOWS) {
			if (config.useJnaForWindows) {
				GlobalWindowsJnaEventOchestrator.of().start();
			} else {
				GlobalWindowsEventOchestrator.of().start();
			}
			return;
		}
		if (OSIdentifier.IS_LINUX) {
			if (USE_X11_ON_LINUX) {
				GlobalX11EventOchestrator.of().start();
				return;
			} else {
				GlobalLinuxEventOchestrator.of().start();
				return;
			}
		}
		if (OSIdentifier.IS_OSX) {
			GlobalOSXEventOchestrator.of().start();
			return;
		}

		throw new RuntimeException("OS not supported.");
	}

	public void stop() {
		if (OSIdentifier.IS_WINDOWS) {
			try {
				if (config.useJnaForWindows) {
					GlobalWindowsJnaEventOchestrator.of().stop();
				} else {
					GlobalWindowsEventOchestrator.of().stop();
				}
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, "Interrupted while stopping.", e);
			}
			return;
		}
		if (OSIdentifier.IS_LINUX) {
			if (USE_X11_ON_LINUX) {
				GlobalX11EventOchestrator.of().stop();
				return;
			} else {
				GlobalLinuxEventOchestrator.of().stop();
				return;
			}
		}
		if (OSIdentifier.IS_OSX) {
			try {
				GlobalOSXEventOchestrator.of().stop();
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, "Interrupted while stopping.", e);
			}
			return;
		}

		throw new RuntimeException("OS not supported.");
	}
}
