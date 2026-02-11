package frost3d;

import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CROSSHAIR_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_IBEAM_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_NOT_ALLOWED_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_POINTING_HAND_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZE_ALL_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZE_EW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZE_NESW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZE_NS_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZE_NWSE_CURSOR;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwDestroyCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import frost3d.enums.CursorType;

public class Input {
	
	private void has_input_this_frame() {
		// To properly detect when single-frame inputs
		// (like 'pressed' or 'released') end, input
		// needs to be checked two frames in a row
		has_input_this_frame = true;
		has_input_next_frame = true;
	}
	
	boolean has_input_this_frame = false;
	boolean has_input_next_frame = false;
	
	public void clearKeys() {
		
		has_input_this_frame = false;
		if (has_input_next_frame && !has_input_this_frame) {
			has_input_next_frame = false;
			has_input_this_frame = true;
		}
		
		// Events
		
		for (int i = 0; i < current_keys.length; i++) current_keys[i] = null;
		
		current_mouse_buttons = new MouseButton[8];
		mouse_scroll_x = 0; mouse_scroll_y = 0;
		changed_focus_state = false;
		any_key_pressed = false;
	}
	
	public record Key(int key, int scancode, int action, int mods) {};
	public record MouseButton(int button, int action, int mods) {};
	
	long			window_identifier		= -1;

	String 			input_string 			= "";
	Key[] 			current_keys 			= new Key[1024];
	MouseButton[] 	current_mouse_buttons 	= new MouseButton[8];
	boolean[] 		down_keys 				= new boolean[1024];
	boolean[] 		down_mouse_buttons 		= new boolean[8];
	double 			mouse_scroll_x 			= 0;
	double 			mouse_scroll_y 			= 0;
	Key				last_key 				= null;
	double			mouse_x					= 0;
	double			mouse_y					= 0;
	Vector2i		mouse_pos				= new Vector2i(0, 0);
	boolean			is_iconified			= false;
	boolean 		is_focused				= true;
	boolean 		changed_focus_state		= true;
	boolean			any_key_pressed			= false;

	public Input(long current_window) {
		
		is_focused = GLFW.glfwGetWindowAttrib(current_window, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(current_window, (_, key, scancode, action, mods) -> {
			has_input_this_frame();
			setKeyWithScancode(key, scancode, action, mods);
			last_key = new Key(key, scancode, action, mods);
			
			if (action == GLFW.GLFW_PRESS) setKeyScancodeDown(scancode, true);
			if (action == GLFW.GLFW_RELEASE) setKeyScancodeDown(scancode, false);
			
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
				if (key == GLFW.GLFW_KEY_BACKSPACE && input_string.length() > 0) 
					input_string = input_string.substring(0, input_string.length()-1);

			if (action == GLFW.GLFW_PRESS) any_key_pressed = true;
			
			//Log.send(scancode + " : " + key);
			//if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
			//	glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});
		
		glfwSetCursorPosCallback(current_window, (_, xpos, ypos) -> {
			has_input_this_frame();
			setMousePos(xpos, ypos);
		});
		
		glfwSetMouseButtonCallback(current_window, (_, button, action, mods) -> {
			has_input_this_frame();
			setMouseButton(button, action, mods);
			
			if (action == GLFW.GLFW_PRESS) setMouseButtonDown(button, true);
			if (action == GLFW.GLFW_RELEASE) setMouseButtonDown(button, false);
		});
		
		glfwSetScrollCallback(current_window, (_, xoffset, yoffset) -> {
			has_input_this_frame();
			setMouseScroll(xoffset, yoffset);
		});

		glfwSetCharCallback(current_window, (_, codepoint) -> {
			has_input_this_frame();
			input_string += (char) codepoint;
		});
		
		GLFW.glfwSetWindowIconifyCallback(current_window, (_, iconified) -> {
			has_input_this_frame();
			is_iconified = iconified;
		});
		
		GLFW.glfwSetWindowFocusCallback(current_window, (_, focused) -> {
			is_focused = focused;
			changed_focus_state = true;
		});
		
		window_identifier = current_window;
	}

	private void setMouseButtonDown(int button, boolean b) {
		down_mouse_buttons[button] = b;
	}

	private void setKeyScancodeDown(int scancode, boolean b) {
		down_keys[scancode] = b;
	}

	private void setKeyWithScancode(int key, int scancode, int action, int mods) {
		current_keys[scancode] = new Key(key, scancode, action, mods);
	}

	private void setMouseScroll(double xoffset, double yoffset) {
		mouse_scroll_x = xoffset;
		mouse_scroll_y = yoffset;
	}

	private void setMouseButton(int button, int action, int mods) {
		current_mouse_buttons[button] = new MouseButton(button, action, mods);
	}

	private void setMousePos(double xpos, double ypos) {
		mouse_x = xpos;
		mouse_y = ypos;
		mouse_pos = new Vector2i((int) xpos, (int) ypos);
	}
	
	// -- Getters -- //
	
	public boolean focused() { return is_focused; }
	
	public boolean any_key_pressed() { return any_key_pressed; }

	public String input_string() { return input_string; }
	
	public int action(int scancode) { return getKeyActionWithScancode(scancode); }
	
	public int getKeyActionWithScancode(int scancode) {
		if (current_keys[scancode] == null) return -1;
		return current_keys[scancode].action;
	}
	
	public int mouseX() { return (int) mouse_x; }
	public int mouseY() { return (int) mouse_y; }

	public Vector2i mousePos() { return mouse_pos; }
	
	public boolean mouseButtonDown(int button) {
		return down_mouse_buttons[button];
	}
	
	public boolean mouseButtonPressed(int button) {
		if (current_mouse_buttons[button] == null) return false;
		return current_mouse_buttons[button].action == GLFW.GLFW_PRESS;
	}
	
	public boolean mouseButtonReleased(int button) {
		if (current_mouse_buttons[button] == null) return false;
		return current_mouse_buttons[button].action == GLFW.GLFW_RELEASE;
	}

	public double scrollX() { return mouse_scroll_x; }
	public double scrollY() { return mouse_scroll_y; }

	public boolean hasInputThisFrame() { return has_input_this_frame; }

	
	/** NOTE: Uses keys, not scancodes ... */
	public boolean keyPressed(int key) {
		return action(GLFW.glfwGetKeyScancode(key)) == GLFW.GLFW_PRESS;
	}

	public boolean keyDown(int key) {
		return down_keys[GLFW.glfwGetKeyScancode(key)];
	}
	
	public boolean keyReleased(int key) {
		return action(GLFW.glfwGetKeyScancode(key)) == GLFW.GLFW_RELEASE;
	}
	
	public boolean keyRepeated(int key) {
		return action(GLFW.glfwGetKeyScancode(key)) == GLFW.GLFW_REPEAT;
	}

	// -- Setters -- //
	
	public void input_string(String s) { input_string = s; }

	public static void setClipboardString(String string) {
		GLFW.glfwSetClipboardString(-1, string);
	}

	public static String getClipboardString() {
		return GLFW.glfwGetClipboardString(-1);
	}
	
	// -- Misc -- //
	
	long current_cursor = -1;
	
	public CursorType current_cursor_type = CursorType.ARROW_CURSOR;
	
	public void cursor(CursorType type) {
		if (current_cursor_type == type) return;
		if (current_cursor 		!= -1  ) glfwDestroyCursor(current_cursor);
		int cursor = GLFW_ARROW_CURSOR;
		if (type == CursorType.		   IBEAM_CURSOR) cursor = GLFW_IBEAM_CURSOR;
		if (type == CursorType.	   CROSSHAIR_CURSOR) cursor = GLFW_CROSSHAIR_CURSOR;
		if (type == CursorType.POINTING_HAND_CURSOR) cursor = GLFW_POINTING_HAND_CURSOR;
		if (type == CursorType.    RESIZE_EW_CURSOR) cursor = GLFW_RESIZE_EW_CURSOR;
		if (type == CursorType.    RESIZE_NS_CURSOR) cursor = GLFW_RESIZE_NS_CURSOR;
		if (type == CursorType.  RESIZE_NWSE_CURSOR) cursor = GLFW_RESIZE_NWSE_CURSOR;
		if (type == CursorType.  RESIZE_NESW_CURSOR) cursor = GLFW_RESIZE_NESW_CURSOR;
		if (type == CursorType.   RESIZE_ALL_CURSOR) cursor = GLFW_RESIZE_ALL_CURSOR;
		if (type == CursorType.  NOT_ALLOWED_CURSOR) cursor = GLFW_NOT_ALLOWED_CURSOR;
		current_cursor 		= glfwCreateStandardCursor(cursor);
		current_cursor_type = type;
		glfwSetCursor(window_identifier, current_cursor);
	}
	
	public CursorType cursor() {
		return current_cursor_type;
	}
	
}
