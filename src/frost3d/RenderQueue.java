package frost3d;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.GL_TEXTURE3;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL40;

import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleTexture;
import frost3d.interfaces.GLMesh;
import frost3d.interfaces.GLTexture;
import frost3d.utility.LimitedStack;
import frost3d.utility.Log;
import frost3d.utility.Rectangle;

public class RenderQueue {

	// -- ++ (  state used for queue()  ) ++ -- //

	HashMap			<String, Integer> 	intuniforms 	= new HashMap<>(); // ex : funny scrolling effect in mplayer
	HashMap			<String, Vector4f> 	vecuniforms 	= new HashMap<>(); // ex : mix_color
	HashMap			<String, Matrix4f> 	matuniforms 	= new HashMap<>(); // ex : transform, world
	LimitedStack	<Rectangle>  		scissors 		= new LimitedStack<>();
	GLMesh 					 			mesh 			= null;
	GLTexture[]							textures		= null;
	GLShaderProgram 					shader			= BuiltinShaders.CORE;
	
	{ scissors.push(null); }
	
	{
		 // intuniforms.put("texture0", 0); // TODO for texture slots
	}
	
	public void push_scissor(Rectangle box) { scissors.push(box); }
	public void pop_scissor(Rectangle box) { scissors.pop(); }

	public void uniform(String name, int v) {
		intuniforms = new HashMap<String, Integer>(intuniforms);
		intuniforms.put(name, v);
	}
	
	public void uniform(String name, Vector4f v) {
		vecuniforms = new HashMap<String, Vector4f>(vecuniforms);
		vecuniforms.put(name, v);
	}
	
	public void uniform(String name, Matrix4f v) {
		matuniforms = new HashMap<String, Matrix4f>(matuniforms);
		matuniforms.put(name, v);
	}
	
	public void clear_uniform(String name) {
		intuniforms = new HashMap<String, Integer>(intuniforms);
		intuniforms.remove(name);
		
		vecuniforms = new HashMap<String, Vector4f>(vecuniforms);
		vecuniforms.remove(name);
		
		matuniforms = new HashMap<String, Matrix4f>(matuniforms);
		matuniforms.remove(name);
	}
	
	public LimitedStack<Rectangle> 	scissors() 					{ return scissors  ; }
	public void 				 	mesh(GLMesh m) 				{ mesh 			= m; }
	public void 					textures(GLTexture... t) 	{ textures 		= t; }
	public void						shader(GLShaderProgram shader) { this.shader = shader; }
	
	public void mix_color(Vector4f color) {
		uniform("mix_color", color);
	}
	
	public void transform(Matrix4f transform) {
		uniform("transform", transform);
	}
	
	public void world_transform(Matrix4f transform) {
		uniform("world_transform", transform);
	}
	
	
	private static record RenderState(
			HashMap<String, Integer> 	intuniforms,
			HashMap<String, Vector4f> 	vecuniforms,
			HashMap<String, Matrix4f> 	matuniforms,
			Rectangle 		scissor,
			GLMesh 			mesh,
			GLTexture[] 	textures,
			GLShaderProgram	shader
			) { }
	
	ArrayList<RenderState> queue = new ArrayList<RenderState>();
	
	public void queue() {
		if (shader == null) throw new Error("Default shader is null. Did a RenderQueue method get called before BuiltinShaders.init()?");
		queue.add(new RenderState(intuniforms, vecuniforms, matuniforms, scissors.peek(), mesh, textures, shader));
	}
	
	// -- ++ (  actual rendering  ) ++ -- //
	
	GLMesh last_mesh = null;
	GLTexture[] last_textures = null;
	Rectangle last_scissor = null;
	GLShaderProgram last_shader = null;
	
	HashMap<String, Integer> 	last_intuniforms;
	HashMap<String, Vector4f> 	last_vecuniforms;
	HashMap<String, Matrix4f> 	last_matuniforms;
	
	public void render() {
		last_mesh = null;
		last_textures = new GLTexture[4];
		last_scissor = null;
		last_shader = null;
		
		last_intuniforms = new HashMap<>();
		last_vecuniforms = new HashMap<>();
		last_matuniforms = new HashMap<>();
		
		for (RenderState state : queue) {

			if (last_shader == null || !last_shader.equals(state.shader)) {
				state.shader.bind();
				last_shader = state.shader;
			}
			
			for (String uniform : state.intuniforms.keySet()) {
				if (!state.intuniforms.get(uniform).equals(last_intuniforms.get(uniform))) {
					GLShaderProgram.uniform(uniform, state.intuniforms.get(uniform));
				}
			}
			last_intuniforms = state.intuniforms;
			
			for (String uniform : state.vecuniforms.keySet()) {
				if (!state.vecuniforms.get(uniform).equals(last_vecuniforms.get(uniform))) {
					GLShaderProgram.uniform(uniform, state.vecuniforms.get(uniform));
				}
			}
			last_vecuniforms = state.vecuniforms;

			for (String uniform : state.matuniforms.keySet()) {
				if (!state.matuniforms.get(uniform).equals(last_matuniforms.get(uniform))) {
					GLShaderProgram.uniform(uniform, state.matuniforms.get(uniform));
				}
			}
			last_matuniforms = state.matuniforms;

			// .. //
			
			if (last_mesh != state.mesh) {
				state.mesh.bind();
				last_mesh = state.mesh;
			}
			
//			if (last_texture != state.texture) {
//				if (state.texture != null) glBindTexture(GL_TEXTURE_2D, state.texture.gltexture());
//				last_texture = state.texture;
//			}
			
			// Textures
			int[] slots = { GL_TEXTURE0, GL_TEXTURE1, GL_TEXTURE2, GL_TEXTURE3};
			if (state.textures != null)
			for (int i = 0; i < state.textures.length; i++) {
				if (i >= 4) Log.send("Attempted to have more than 4 textures");
				
				if (last_textures[i] != state.textures[i]) {
					if (state.textures[i] != null)  {
						bindTexture(slots[i], state.textures[i]);
					} else {
						bindTexture(slots[i], MISSING_TEXTURE);
					}
				}
				
				last_textures[i] = state.textures[i];
			}
			
			if (last_scissor != state.scissor) {
				if (state.scissor != null) {
//					GL40.glScissor(
//							state.scissor().left(), 
//							DEMO_GUI0.current_window.height - state.scissor().bottom(), 
//							(state.scissor().right()-state.scissor().left()), 
//							(state.scissor().bottom()-state.scissor().top()));
					GL40.glScissor(state.scissor().left(), 	state.scissor().top(), 
								   state.scissor().width(), state.scissor().height());
					GL40.glEnable(GL40.GL_SCISSOR_TEST);
				} else {
					//GL40.glScissor(0, 0, DEMO_GUI0.current_window.width, DEMO_GUI0.current_window.height);
					GL40.glDisable(GL40.GL_SCISSOR_TEST);
				}
				last_scissor = state.scissor;
			}

			GL40.glDrawElements(GL_TRIANGLES, state.mesh.index_count(), GL_UNSIGNED_INT, 0);
		}
		
		intuniforms.clear();
		vecuniforms.clear();
		matuniforms.clear();
		queue.clear();
	}

	// -- TODO: probably move these somewhere else-- //
	
	// https://youtu.be/q69-VhhSY3I
	public static SimpleTexture MISSING_TEXTURE;
	static {
		BufferedImage img = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
			img.setRGB(0, 0, 0xFFFF00FF);
			img.setRGB(1, 0, 0xFF000000);
			img.setRGB(0, 1, 0xFF000000);
			img.setRGB(1, 1, 0xFFFF00FF);
		MISSING_TEXTURE = new SimpleTexture(img);
			MISSING_TEXTURE.bind();
			GL40.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			GL40.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}

	private static void bindTexture(int slot, GLTexture texture) {
		GL40.glActiveTexture(slot);
		glBindTexture(GL_TEXTURE_2D, texture.gltexture());
	}

	public int size() { return queue.size(); }
	
	public Rectangle current_scissor() { return scissors.peek(); }
}
