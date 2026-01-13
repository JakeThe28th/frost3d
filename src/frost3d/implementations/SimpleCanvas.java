package frost3d.implementations;

import static org.lwjgl.opengl.GL11.glViewport;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import frost3d.Framebuffer;
import frost3d.GLShaderProgram;
import frost3d.GLState;
import frost3d.RenderQueue;
import frost3d.Shapes;
import frost3d.data.BuiltinShaders;
import frost3d.enums.IconType;
import frost3d.interfaces.F3DCanvas;
import frost3d.interfaces.F3DIconRenderer;
import frost3d.interfaces.F3DTextRenderer;
import frost3d.interfaces.GLMesh;
import frost3d.interfaces.GLTexture;
import frost3d.utility.Rectangle;

public class SimpleCanvas implements F3DCanvas {
	//turn this into generic canvas and make gui use ICanvas an interface
	
		// -- ++ (  effectively final state,  ) ++ -- //

		private RenderQueue renderqueue = new RenderQueue();
		
		// -- ++ ( infrequently changed state ) ++ -- //
		
		F3DTextRenderer textrenderer;
		F3DIconRenderer iconrenderer;
		
		Framebuffer framebuffer = null;
		int width 			= -1;
		int height 			= -1;
		public int width() { return width; }
		public int height() { return height; }

		private GLShaderProgram current_shader = BuiltinShaders.MONOCOLORED_TEXTURED_UNSHADED;
		
		public void textrenderer(F3DTextRenderer v) { this.textrenderer = v; }
		public F3DTextRenderer textrenderer() { return this.textrenderer; }
		
		public void iconrenderer(F3DIconRenderer v) { this.iconrenderer = v; }
		public F3DIconRenderer iconrenderer() { return this.iconrenderer; }
		
		public void framebuffer	(Framebuffer v) { this.framebuffer 	= v; }
		public Framebuffer framebuffer	() { return this.framebuffer; }

		public void size		(int w, int h) { 
			this.width = w;
			this.height = h;
			gui_bounds = new Rectangle(0, 0, width, height);
			world_transform(new Matrix4f().ortho(0, width, height, 0, -1024f, 1024f));
			}
		
		public void adopt(Framebuffer framebuffer) {
			size(framebuffer.width(), framebuffer.height());
			framebuffer(framebuffer);
		}
		
		Rectangle gui_bounds = new Rectangle(0, 0, width, height);
		public Rectangle size() { return gui_bounds; }
		
		// -- ++ (  frequently changed state  ) ++ -- //
		
		/** Saves the current scissor and then sets the new scissor to this. */
		public void push_scissor(Rectangle box) {
			renderqueue.push_scissor(new Rectangle(
					box.left(), height-box.bottom(), 
					box.right(), height-box.top()));
		}
		
		/** Reverts to the previous scissor box. */
		public void pop_scissor() {
			renderqueue.pop_scissor(gui_bounds);

		}
		
		public void push_scissor(int left, int top, int right, int bottom) {
			push_scissor(new Rectangle(left, top, right, bottom));
		}
		
		Vector4f color = new Vector4f();
		Vector4f clear_color = new Vector4f(1,1,1,1);
		
		Matrix4f world_transform;

		public void color(Vector4f color) { this.color = color; }

		public void world_transform(Matrix4f mat) {
			world_transform = mat;
		}
		
		// -- **  ** -- //

		public void draw_frame() { draw_frame(true); }
		public void draw_frame(boolean clear) {
			// clear the framebuffer
			if (framebuffer != null) framebuffer.bind();
			if (framebuffer == null && GLState.bindFramebuffer(0)) glViewport(0,0, width, height);
			if (clear) GLState.clearColor(clear_color.x, clear_color.y, clear_color.z, clear_color.w);
			if (clear) GLState.clear();
			
			renderqueue.render();			
		}
		
		@Override public void 		uniform(String name, int 	  v) { renderqueue.uniform(name, v); }
		@Override public void 		uniform(String name, Vector4f v) { renderqueue.uniform(name, v); }
		@Override public void 		uniform(String name, Matrix4f v) { renderqueue.uniform(name, v); }
		@Override public void clear_uniform(String name			   ) { renderqueue.clear_uniform(name);}
		
		// -- ** Drawing API ** -- //
		
		public void rect(float left, float top, float right, float bottom, float depth) {
			Shapes.rect(this, (int) left, (int) top, (int) right, (int) bottom, (int) depth);
		}
		
		public void rect(Rectangle bounds, int depth) {
			Shapes.rect(this, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), depth);
		}

		public void rect(int left, int top, int right, int bottom, int depth) {
			Shapes.rect(this, left, top, right, bottom, depth);
		}
		
		public void rect(int left, int top, int right, int bottom, int depth, GLTexture texture) {
			Shapes.rect(this, left, top, right, bottom, depth, texture);
		}
		

		public void dot(int x, int y, int depth, int radius) {
			Shapes.dot(this, x, y, depth, radius);
		}
		
		public void text(int x, int y, int depth, String text) {
			textrenderer.text(this, x, y, depth, text, renderqueue.current_scissor());
		}
		
		public void icon(int x, int y, int depth, IconType icon) {
			iconrenderer.icon(this, x, y, depth, icon);
		}
		
		@Override
		public void queue(GLMesh mesh, Matrix4f transform, GLTexture... textures) {
			queue(mesh, transform, world_transform, current_shader, textures);
		}
		
		@Override
		public void queue(GLMesh mesh, Matrix4f transform, GLShaderProgram shader, GLTexture... textures) {
			queue(mesh, transform, world_transform, shader, textures);
		}

		
		@Override
		public void queue(GLMesh mesh, Matrix4f transform, Matrix4f world_transform, GLShaderProgram shader, GLTexture... textures) {
			// default
			renderqueue.mix_color(color);
			
			// specific
			renderqueue.mesh(mesh);
			renderqueue.transform(transform);
			renderqueue.world_transform(world_transform);
			renderqueue.textures(textures);
			renderqueue.shader(shader);
			renderqueue.queue();
		}

		@Override
		public void clear_color(float r, float g, float b, float a) {
			clear_color = new Vector4f(r,g,b,a);
		}
		
		public void clear_color(Vector4f color) {
			clear_color = color;
		}
		
		public int queue_size() { return renderqueue.size(); }
	
}
