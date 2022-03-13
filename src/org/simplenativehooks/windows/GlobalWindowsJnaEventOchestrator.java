package org.simplenativehooks.windows;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.simplenativehooks.NativeHookGlobalEventPublisher;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.HOOKPROC;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;

public class GlobalWindowsJnaEventOchestrator {

	private static final Logger LOGGER = Logger.getLogger(GlobalWindowsJnaEventOchestrator.class.getName());

	private boolean useJavaAwtToReportMousePositionOnWindows;

	private static final GlobalWindowsJnaEventOchestrator INSTANCE = new GlobalWindowsJnaEventOchestrator();

	public static GlobalWindowsJnaEventOchestrator of() {
		return INSTANCE;
	}

	private GlobalWindowsJnaEventOchestrator() {}

	public void setUseJavaAwtToReportMousePositionOnWindows(boolean use) {
		this.useJavaAwtToReportMousePositionOnWindows = use;
	}

	public interface LowLevelMouseProc extends HOOKPROC {
		LRESULT callback(int nCode, WPARAM wParam, MOUSEHOOKSTRUCT lParam);
	}

	public static class MOUSEHOOKSTRUCT extends Structure {
		public class ByReference extends MOUSEHOOKSTRUCT implements Structure.ByReference {
		};

		public POINT pt;
		public HWND hwnd;
		public int wHitTestCode;
		public ULONG_PTR dwExtraInfo;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("pt", "hwnd", "wHitTestCode", "dwExtraInfo");
		}
	}

	private HHOOK mouseHHK, keyboardHHK; // Hook handlers.
	private LowLevelMouseProc mouseHook;
	private LowLevelKeyboardProc keyboardHook;

	private boolean done;
	public Thread driverThread;

	private void setHook() {
		HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
		mouseHHK = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_MOUSE_LL, mouseHook, hMod, 0);
		keyboardHHK = User32.INSTANCE.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0);
	}

	private void unhook() {
		User32.INSTANCE.UnhookWindowsHookEx(keyboardHHK);
		User32.INSTANCE.UnhookWindowsHookEx(mouseHHK);
	}

	public void start() {
		keyboardHook = new LowLevelKeyboardProc() {
			@Override
			// See reference at:
			// http://msdn.microsoft.com/en-us/library/windows/desktop/ms644985(v=vs.85).aspx
			public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT lParam) {
				if (nCode >= 0) {
					int w = wParam.intValue();
					// w can have the following values.
					// WinUser.WM_KEYDOWN, WinUser.WM_SYSKEYDOWN (for Alt key).
					// WinUser.WM_KEYUP, WinUser.WM_SYSKEYUP

					NativeHookGlobalEventPublisher.of().publishKeyEvent(WindowsNativeKeyEvent.of(lParam.vkCode, w));
				}

				Pointer ptr = lParam.getPointer();
				long peer = Pointer.nativeValue(ptr);
				return User32.INSTANCE.CallNextHookEx(keyboardHHK, nCode, wParam, new LPARAM(peer));
			}
		};

		mouseHook = new LowLevelMouseProc() {
			@Override
			// See reference at:
			// http://msdn.microsoft.com/en-us/library/windows/desktop/ms644986(v=vs.85).aspx
			public LRESULT callback(int nCode, WPARAM wParam, MOUSEHOOKSTRUCT lParam) {
				if (nCode >= 0) {
					int x = lParam.pt.x;
					int y = lParam.pt.y;

					if (useJavaAwtToReportMousePositionOnWindows) {
						Point p = MouseInfo.getPointerInfo().getLocation();
						x = p.x;
						y = p.y;
					}

					NativeHookGlobalEventPublisher.of().publishMouseEvent(WindowsNativeMouseEvent.of(x, y, wParam.intValue()));
				}

				Pointer ptr = lParam.getPointer();
				long peer = Pointer.nativeValue(ptr);
				return User32.INSTANCE.CallNextHookEx(mouseHHK, nCode, wParam, new LPARAM(peer));
			}
		};

		done = false;
		driverThread = new Thread(){
			@Override
			public void run() {
				setHook();

				MSG msg = new MSG();
				// Message loop. This needs to be run in the same thread as the one
				// calling setHook().
				// In fact, the while loop is not executed once, the code will block
				// at in the GetMessage function most of the time.
				while (!done) {
					int result = User32.INSTANCE.GetMessage(msg, null, 0, 0);
					if (result == 0) {
						break;
					}

					if (result == -1) {
						LOGGER.severe("GetMessage has error.");
						done = true;
						unhook();
						break;
					} else {
						User32.INSTANCE.TranslateMessage(msg);
						User32.INSTANCE.DispatchMessage(msg);
					}
				}
			}
		};
		LOGGER.info("Starting JNA even hook thread...");
		driverThread.setDaemon(true);
		driverThread.start();
		LOGGER.info("JNA even hook thread started.");
	}

	public void stop() throws InterruptedException {
		LOGGER.info("Stopping JNA even hook.");
		done = true;
		unhook();
		LOGGER.info("Stopped JNA even hook.");
	}
}
