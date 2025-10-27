package frost3d;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;

import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL40;

import frost3d.interfaces.GLMesh;
import frost3d.interfaces.GLTexture;
import frost3d.utility.LimitedStack;
import frost3d.utility.Rectangle;

public class RenderQueue {

	// -- ++ (  state used for queue()  ) ++ -- //

	HashMap			<String, Integer> 	intuniforms 	= new HashMap<>(); // ex : funny scrolling effect in mplayer
	HashMap			<String, Vector4f> 	vecuniforms 	= new HashMap<>(); // ex : mix_color
	HashMap			<String, Matrix4f> 	matuniforms 	= new HashMap<>(); // ex : transform, world
	LimitedStack	<Rectangle>  		scissors 		= new LimitedStack<>();
	GLMesh 					 			mesh 			= null;
	GLTexture							texture			= null;
	String 								shader			= Shaders.CORE;
	
	{ scissors.push(null); }
	
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
	
	public LimitedStack<Rectangle> 	scissors() 		{ return scissors  ; }
	public void 				 	mesh(GLMesh m) 	{ mesh 			= m; }
	public void 					texture(GLTexture t) { texture 		= t; }
	public void						shader(String shader) { this.shader = shader; }
	
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
			Rectangle 	scissor,
			GLMesh 		mesh,
			GLTexture 	texture,
			String		shader
			) { }
	
	ArrayList<RenderState> queue = new ArrayList<RenderState>();
	
	public void queue() {
		queue.add(new RenderState(intuniforms, vecuniforms, matuniforms, scissors.peek(), mesh, texture, shader));
	}
	
	// -- ++ (  actual rendering  ) ++ -- //
	
	GLMesh last_mesh = null;
	GLTexture last_texture = null;
	Rectangle last_scissor = null;
	String last_shader = null;
	
	public void render() {
		last_mesh = null;
		last_texture = null;
		last_scissor = null;
		last_shader = null;
		
		for (RenderState state : queue) {

			if (last_shader == null || !last_shader.equals(state.shader)) {
				Shaders.bind(state.shader);
				last_shader = state.shader;
			}
			
			for (String uniform : state.intuniforms.keySet()) {
				Shaders.uniform(uniform, state.intuniforms.get(uniform));
			}
			
			for (String uniform : state.vecuniforms.keySet()) {
				Shaders.uniform(uniform, state.vecuniforms.get(uniform));
			}
			
			for (String uniform : state.matuniforms.keySet()) {
				Shaders.uniform(uniform, state.matuniforms.get(uniform));
			}
			
			// .. //
			
			if (last_mesh != state.mesh) {
				state.mesh.bind();
				last_mesh = state.mesh;
			}
			
			if (last_texture != state.texture) {
				if (state.texture != null) glBindTexture(GL_TEXTURE_2D, state.texture.gltexture());
				last_texture = state.texture;
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

}
