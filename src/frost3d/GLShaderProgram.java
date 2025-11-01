package frost3d;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL40;

/** (11/1/2025 TODO: this comment is probably not relevant anymore)
 * Replacement for half of the 'Shaders' class, <br>
 * to bring the class more in line with Texture vs GLTexture.<br>
 * Static shader access via namespaced ID was moved into ResourceManager <br>
 */
public class GLShaderProgram {
	
	int program; // Shader program reference
	Long context; // The context this shader was compiled in
	
	public void bind() { glUseProgram(program); GLState.current_shader = program; }
	public void free() { glDeleteProgram(program); }
	
	public GLShaderProgram(String vertex_source, String fragment_source) {
		
		int vertex_shader = compile(GL40.GL_VERTEX_SHADER, vertex_source);
		int fragment_shader = compile(GL40.GL_FRAGMENT_SHADER, fragment_source);
		
		GL40.glEnable(GL40.GL_BLEND);  
		GL40.glBlendFunc(GL40.GL_SRC_ALPHA, GL40.GL_ONE_MINUS_SRC_ALPHA);
		
		int shader_program = GL40.glCreateProgram();
		GL40.glAttachShader(shader_program, vertex_shader);
		GL40.glAttachShader(shader_program, fragment_shader);
		GL40.glLinkProgram(shader_program);
		
		// linking failed
	 	if (GL40.glGetProgrami(shader_program, GL40.GL_LINK_STATUS) == 0) {
	 		 String info_log = GL40.glGetProgramInfoLog(shader_program, 512);
	 		 throw new RuntimeException("Shader linking failed: " + info_log);
	 	}
		
	 	GL40.glDeleteShader(vertex_shader);
	 	GL40.glDeleteShader(fragment_shader);
		
		this.program = shader_program;
		this.context = GLState.current_context;
	}
	

	/** Creates an openGL shader from source code,
	    used only in the constructor. I'd make it 
	    a sub-method if that was a feature Java 
	    had... */
	private static int compile(int type, String source) {
		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		glCompileShader(shader);
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
		    String info_log = glGetShaderInfoLog(shader, 512);
		    throw new RuntimeException("Shader compilation failed : " + info_log);
		}
		return shader;
	}
	

	public static void uniform(String uniform, Matrix4f matrix) {
		GL40.glUniformMatrix4fv(
				GL40.glGetUniformLocation(GLState.current_shader, uniform), 
				false, 
				floats(matrix)
			);
	}

	public static void uniform(String uniform, Vector4f vector) {
		GL40.glUniform4f(
				GL40.glGetUniformLocation(GLState.current_shader, uniform), 
				vector.x, 
				vector.y, 
				vector.z, 
				vector.w
			);
	}
	
	public static void uniform(String uniform, int integer) {
		GL40.glUniform1i(
				GL40.glGetUniformLocation(GLState.current_shader, uniform), 
				integer
			);
	}
	
	/** Returns an array of floats from a given matrix.
	 *  ...Because that takes more than one line,
	 *  for some reason... */
	private static float[] floats(Matrix4f matrix) {
		float[] floats = new float[4*4];
		matrix.get(floats);
		return floats;
	}

}
