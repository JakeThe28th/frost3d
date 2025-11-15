package frost3d.data;

import frost3d.GLShaderProgram;
import frost3d.utility.Log;

public class BuiltinShaders {
	
	public static GLShaderProgram CORE;
	public static GLShaderProgram SCREEN;
	public static GLShaderProgram GUI;
	public static GLShaderProgram MONOCOLORED_TEXTURED_UNSHADED;

	public static void init() {
		try {
					
		CORE = new GLShaderProgram("""
					#version 330 core
					layout (location = 0) in vec3 v_position;
					void main() { gl_Position = vec4(v_position, 1.0); }
				""", """
					#version 330 core
					out vec4 FragColor;
					void main() { FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f); } 
				""");
		
		CORE.bind();
		
		GUI = new GLShaderProgram("""
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
		
		MONOCOLORED_TEXTURED_UNSHADED = new GLShaderProgram("""
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

				uniform vec4 texcoord_offset = vec4(0, 0, 0, 0);

				void main() {
					vec2 t_offset = vec2(texcoord_offset.x, texcoord_offset.y);
					FragColor = texture(texture_image, f_texcoord + t_offset) * mix_color;
					if (FragColor.a < 0.001) discard;
				} 
			""");
			 	
	 	SCREEN = new GLShaderProgram("""
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
	 	
		} catch (Exception e) {
			Log.trace(e);
		}
			
	}
	
}
