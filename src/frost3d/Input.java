package frost3d;

import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import org.lwjgl.glfw.GLFW;

public class Input {
	
	boolean has_input_this_frame = false;

	public void clearKeys() {
		has_input_this_frame = false;
		// Events
		current_keys = new Key[1024];
		current_mouse_buttons = new MouseButton[8];
		mouse_scroll_x = 0; mouse_scroll_y = 0;
	}
	
	public record Key(int key, int scancode, int action, int mods) {};
	public record MouseButton(int button, int action, int mods) {};

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
	boolean			is_iconified			= false;

	public Input(long current_window) {
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(current_window, (_, key, scancode, action, mods) -> {
			has_input_this_frame = true;
			setKeyWithScancode(key, scancode, action, mods);
			last_key = new Key(key, scancode, action, mods);
			
			if (action == GLFW.GLFW_PRESS) setKeyScancodeDown(scancode, true);
			if (action == GLFW.GLFW_RELEASE) setKeyScancodeDown(scancode, false);
			
			if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
				if (key == GLFW.GLFW_KEY_BACKSPACE && input_string.length() > 0) 
					input_string = input_string.substring(0, input_string.length()-1);

			
			//Log.send(scancode + " : " + key);
			//if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
			//	glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});
		
		glfwSetCursorPosCallback(current_window, (_, xpos, ypos) -> {
			has_input_this_frame = true;
			setMousePos(xpos, ypos);
		});
		
		glfwSetMouseButtonCallback(current_window, (_, button, action, mods) -> {
			has_input_this_frame = true;
			setMouseButton(button, action, mods);
			
			if (action == GLFW.GLFW_PRESS) setMouseButtonDown(button, true);
			if (action == GLFW.GLFW_RELEASE) setMouseButtonDown(button, false);
		});
		
		glfwSetScrollCallback(current_window, (_, xoffset, yoffset) -> {
			has_input_this_frame = true;
			setMouseScroll(xoffset, yoffset);
		});

		glfwSetCharCallback(current_window, (_, codepoint) -> {
			has_input_this_frame = true;
			input_string += (char) codepoint;
		});
		
		GLFW.glfwSetWindowIconifyCallback(current_window, (_, iconified) -> {
			has_input_this_frame = true;
			is_iconified = iconified;
		});
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
	}
	
	// -- Getters -- //
	
	public int action(int scancode) { return getKeyActionWithScancode(scancode); }
	
	public int getKeyActionWithScancode(int scancode) {
		if (current_keys[scancode] == null) return -1;
		return current_keys[scancode].action;
	}
	
	public int mouseX() { return (int) mouse_x; }
	public int mouseY() { return (int) mouse_y; }
	
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

}
