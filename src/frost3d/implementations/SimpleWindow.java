package frost3d.implementations;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import frost3d.GLState;
import frost3d.Input;
import frost3d.interfaces.F3DWindow;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SimpleWindow implements F3DWindow {
	
	Input 				input;
	public Input input() { return input; }
	
	private long 		window;
	public int 			width;
	public int 			height;
	
	private boolean 	should_close = false;
	public boolean 		decorated 	 = true;

	
	public int width() { return width; }
	public int height() { return height; }

	public boolean should_close() {
		return glfwWindowShouldClose(window);
	}
	
	public void tick() {
		glfwSwapBuffers(window); // swap the color buffers
		
		input.clearKeys();

		// Poll for window events.
		glfwPollEvents();
	}

	public SimpleWindow(int w, int h, String title) {
		this(w, h, title, true, 0);
	}
	
	public SimpleWindow(int w, int h, String title, boolean decorated, int anti_aliasing_samples) {
		
		width = w;
		height = h;
		
		onWindowResize();
		
		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
		
		if (!decorated) {
			glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
			this.decorated = false;
		}
		
		if (anti_aliasing_samples > 1) {
			glfwWindowHint(GLFW_SAMPLES, anti_aliasing_samples);
		}

		// Create the window
		window = glfwCreateWindow(w, h, title, NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
//		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
//			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
//				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
//		});
		
		glfwSetWindowSizeCallback(window, (_, width, height) -> {
			this.width = width;
			this.height = height;
			viewport();
			onWindowResize();
		});

		center();

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
		
		// Set the clear color
		glClearColor(0.20f, 0.0f, 0.0f, 0.0f);

		input = new Input(identifier());
		
		if (anti_aliasing_samples > 1) {
			GL13.glEnable(GL13.GL_MULTISAMPLE);
		}
	}
	
	public void end() {
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
	}

	public int framebuffer() {
		// TODO Auto-generated method stub <-- actually the default is what i wanted :)
		return 0;
	}
	
	public void viewport() {
		glViewport(0,0, width, height);
	}
	
	@Override public long identifier() { return window; }
	@Override public void bind() {
		GLState.bindFramebuffer(0);
		viewport();
	}
	
	// ... //

	public void onWindowResize() { }
	

	protected boolean shouldClose() {
		return (should_close || GLFW.glfwWindowShouldClose(window));
	}

	public void title(String title) {
		GLFW.glfwSetWindowTitle(window, title);
	}

	public void setVsync(boolean value) {
		glfwSwapInterval(value ? 1 : 0);		
	}
	
	public void setDecorated(boolean b) {
		decorated = b;
		if (b) {
			GLFW.glfwSetWindowAttrib(identifier(), GLFW_DECORATED, GLFW_TRUE);
		} else {
			GLFW.glfwSetWindowAttrib(identifier(), GLFW_DECORATED, GLFW_FALSE);
		}
	}
	public void center() {
		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically
	}

}
