package org.simplenativehooks;

import java.io.IOException;
import java.net.URISyntaxException;

import org.simplenativehooks.events.NativeKeyEvent;
import org.simplenativehooks.events.NativeMouseEvent;
import org.simplenativehooks.staticResources.BootStrapResources;
import org.simplenativehooks.utilities.Function;

public class Example {
	public static void main(String[] args) throws InterruptedException {
		/* Extracting resources */
		try {
			BootStrapResources.extractResources();
		} catch (IOException | URISyntaxException e) {
			System.out.println("Cannot extract bootstrap resources.");
			e.printStackTrace();
			System.exit(2);
		}
		/* Initializing global hooks */
		NativeHookInitializer.of().start();

		/* Set up callbacks */
		NativeKeyHook key = NativeKeyHook.of();
		key.setKeyPressed(new Function<NativeKeyEvent, Boolean>() {
			@Override
			public Boolean apply(NativeKeyEvent d) {
				System.out.println("Key pressed: " + d.getKey());
				return true;
			}
		});
		key.setKeyReleased(new Function<NativeKeyEvent, Boolean>() {
			@Override
			public Boolean apply(NativeKeyEvent d) {
				System.out.println("Key released: " + d.getKey());
				return true;
			}
		});
		key.startListening();

		NativeMouseHook mouse = NativeMouseHook.of();
		mouse.setMousePressed(new Function<NativeMouseEvent, Boolean>() {
			@Override
			public Boolean apply(NativeMouseEvent d) {
				System.out.println("Mouse pressed button " + d.getButton() + " at " + d.getX() + ", " + d.getY());
				return true;
			}
		});
		mouse.setMouseReleased(new Function<NativeMouseEvent, Boolean>() {
			@Override
			public Boolean apply(NativeMouseEvent d) {
				System.out.println("Mouse released button " + d.getButton() + " at " + d.getX() + ", " + d.getY());
				return true;
			}
		});
		mouse.setMouseMoved(new Function<NativeMouseEvent, Boolean>() {
			@Override
			public Boolean apply(NativeMouseEvent d) {
				System.out.println("Mouse moved to " + d.getX() + ", " + d.getY());
				return true;
			}
		});
		mouse.startListening();

		/* Wait for testing before shutting down. */
		Thread.sleep(5000);

		/* Clean up */
		NativeHookInitializer.of().stop();
	}
}
