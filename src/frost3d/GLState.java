package frost3d;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL40;

public class GLState {

	public static long current_context;	
	public static GLShaderProgram current_shader;
	
	public static void destroyContext(long window_identifier) { /* Shaders.destroyContext(window_identifier); */ }
	
	/** Clear the currently bound framebuffer. */
	public static void clear() { glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); }
	public static void clearColor(float r, float g, float b, float a)	{ glClearColor(r, g, b, a);	}
	public static void clearColor(Vector4f color) { glClearColor(color.x, color.y, color.z, color.w);	}

	public static void initializeGLFW() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");	
		
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");
	}
	
	public static void endGLFW() {
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public static int last_bound_framebuffer = 0;

	public static void bindFramebuffer(int v) {
		if (v != last_bound_framebuffer) {
			GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, v);
			last_bound_framebuffer = v;
		}
	}
	
}
