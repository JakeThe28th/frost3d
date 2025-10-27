package frost3d;

import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL40;

public class Shaders {

	// Store current context
	
	// Compile shaders
	
	// Uniforms
	
	// Load from NBT
	
	// etc
	
	// TODO ^^^ //
	
	public static final String CORE = "core";
	public static final String GUI = "gui";
	public static final String MONOCOLORED_TEXTURED_UNSHADED = "monocolored_textured_unshaded";
	public static final String SCREEN = "screen";

	//todo registry stuff decoupling
	static long current_context = 0; // TODO -- Handle windows switching contexts...
	static int current_shader = -1;
	
	static HashMap<String, Integer> shaders = new HashMap<>();
	
	public static void bind(String name) {
		bind(shaders.get(name));
	}
	
	public static void register(String name, String vertex, String fragment) {
		
		int vertex_shader = create(GL40.GL_VERTEX_SHADER, vertex);
		int fragment_shader = create(GL40.GL_FRAGMENT_SHADER, fragment);
		
		GL40.glEnable(GL40.GL_BLEND);  
		GL40.glBlendFunc(GL40.GL_SRC_ALPHA, GL40.GL_ONE_MINUS_SRC_ALPHA);
		
		int shader = GL40.glCreateProgram();
		GL40.glAttachShader(shader, vertex_shader);
		GL40.glAttachShader(shader, fragment_shader);
		GL40.glLinkProgram(shader);
		
		// linking failed
	 	if (GL40.glGetProgrami(shader, GL40.GL_LINK_STATUS) == 0) {
	 		 String info_log = GL40.glGetProgramInfoLog(shader, 512);
	 		 throw new RuntimeException("Shader linking failed: " + info_log);
	 	}
		
	 	GL40.glDeleteShader(vertex_shader);
	 	GL40.glDeleteShader(fragment_shader);
	 	
	 	shaders.put(name, shader);
	}
	
	public static void uniform(String uniform, Matrix4f matrix) {
		GL40.glUniformMatrix4fv(
				GL40.glGetUniformLocation(shader(), uniform), 
				false, 
				floats(matrix)
			);
	}

	public static void uniform(String uniform, Vector4f vector) {
		GL40.glUniform4f(
				GL40.glGetUniformLocation(shader(), uniform), 
				vector.x, 
				vector.y, 
				vector.z, 
				vector.w
			);
	}
	
	public static void uniform(String uniform, int integer) {
		GL40.glUniform1i(
				GL40.glGetUniformLocation(shader(), uniform), 
				integer
			);
	}
	
	// --==+  internal methods  +==-- //

	/** Creates an openGL shader from source code,
	    used only in shader(). I'd make it a sub-method
	    if that was a feature Java had... */
	private static int create(int type, String source) {
		int shader = GL40.glCreateShader(type);
	 	GL40.glShaderSource(shader, source);
	 	GL40.glCompileShader(shader);
	 	// compiling failed
	 	if (GL40.glGetShaderi(shader, GL40.GL_COMPILE_STATUS) == 0) {
	 		 String info_log = glGetShaderInfoLog(shader, 512);
	 		 throw new RuntimeException("Shader compilation failed: " + info_log);
	 	}
	 	
		return shader;
	}
	
	private static void bind(int shader) {
		GL40.glUseProgram(shader);
		current_shader = shader;
	}
	
	private static int shader() {
		if (current_shader == -1) throw new RuntimeException("current_shader is not bound");
		return current_shader;
	}
	
	/** Returns an array of floats from a given matrix.
	 *  ...Because that takes more than one line,
	 *  for some reason... */
	private static float[] floats(Matrix4f matrix) {
		float[] floats = new float[4*4];
		matrix.get(floats);
		return floats;
	}
	
	// -- -- -- //
	
	public static void init() {
		
		register("core", """
					#version 330 core
					layout (location = 0) in vec3 v_position;
					void main() { gl_Position = vec4(v_position, 1.0); }
				""", """
					#version 330 core
					out vec4 FragColor;
					void main() { FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f); } 
				""");
		
		register("gui", """
					#version 330 core
					layout (location = 0) in vec3 v_position;
					layout (location = 1) in vec2 v_texcoord;

					uniform mat4 world_transform;	
					uniform mat4 transform;											
					out vec2 f_texcoord;

					void main() {
						f_texcoord = v_texcoord;
					    gl_Position = world_transform * transform * vec4(v_position, 1.0);
					}
				""", """
					#version 330 core
					out vec4 FragColor;

					uniform sampler2D texture_image;
					in vec2 f_texcoord;
					uniform vec4 mix_color;

					// (for the scrolling song titles)
					uniform int first_fade_transparent_x = 0;
					uniform int first_fade_opaque_x = 0;
					uniform int second_fade_transparent_x = 0;
					uniform int second_fade_opaque_x = 0;
					void main() {
					    //FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
						FragColor = texture(texture_image, f_texcoord) * mix_color;

						if (FragColor.a < 0.001) discard;

						float t = 0;
						float blend_alpha = 1;
						if (gl_FragCoord.x < second_fade_opaque_x) {
							// Fading (for the left of scrolling song titles)
							t = (gl_FragCoord.x - first_fade_transparent_x) / (first_fade_opaque_x - first_fade_transparent_x);
							blend_alpha = clamp(t, 0, 1);
							if (blend_alpha < 0.001) discard;
						} else {
							// Fading (for the right of scrolling song titles)
							t = (gl_FragCoord.x - second_fade_transparent_x) / (second_fade_opaque_x - second_fade_transparent_x);
							blend_alpha = clamp(t, 0, 1);
							if (blend_alpha < 0.001) discard;
						}

						FragColor.a = FragColor.a * blend_alpha;
					} 
				""");
		
		register("monocolored_textured_unshaded", """
				#version 330 core
				layout (location = 0) in vec3 v_position;
				layout (location = 1) in vec2 v_texcoord;

				uniform mat4 world_transform;	
				uniform mat4 transform;											
				out vec2 f_texcoord;

				void main() {
					f_texcoord = v_texcoord;
				    gl_Position = world_transform * transform * vec4(v_position, 1.0);
				}
			""", """
				#version 330 core
				out vec4 FragColor;

				uniform sampler2D texture_image;
				in vec2 f_texcoord;
				uniform vec4 mix_color;

				void main() {
					FragColor = texture(texture_image, f_texcoord) * mix_color;
					if (FragColor.a < 0.001) discard;
				} 
			""");
		
	 	bind("core");
	 	
	 	register("screen", """
		 			#version 330 core
					layout (location = 0) in vec3 aPos;
					layout (location = 1) in vec2 aTexCoord;
			
					out vec2 TexCoord;
			
					void main()
					{
						gl_Position = vec4(aPos, 1.0);
						TexCoord = aTexCoord;
					}
	 			""", """
	 				#version 330 core
					out vec4 FragColor;
					  
					in vec2 TexCoord;
			
					uniform sampler2D fragment_texture;
			
					void main()
					{
						vec4 texColor = texture(fragment_texture, TexCoord);
						if(texColor.a < 0.1)
							discard;
						FragColor = texColor;
					}
	 			""");
			
	}
	
}
