package frost3d.interfaces;

import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import frost3d.Framebuffer;
import frost3d.utility.Rectangle;

public interface F3DCanvas {
	
	public void textrenderer(F3DTextRenderer v);
	public F3DTextRenderer textrenderer();

	public void framebuffer	(Framebuffer f);
	public void size		(int w, int h);
	
	// -- ++ STATE ++ -- //
	
	/** Saves the current scissor and then sets the new scissor to this. */
	public void push_scissor(Rectangle box);
	
	/** Saves the current scissor and then sets the new scissor to this. */
	public void push_scissor(int left, int top, int right, int bottom);

	/** Reverts to the previous scissor box. */
	public void pop_scissor();
	
	/** Sets the current color to this */
	public void color(Vector4f color);
	
	public void clear_color(float r, float g, float b, float a);
	
	// -- ++ DRAWING ++ -- //
	
	public void rect(int left, int top, int right, int bottom, int depth);
	
	public void rect(int left, int top, int right, int bottom, int depth, GLTexture texture);
	
	public void text(int x, int y, int depth, String text);
	
	// -- ++  ...  ++ -- //

	/** Finalizes all of the provided draw commands,
	 *  and renders them to the framebuffer. */
	public void draw_frame();

	public void queue(GLMesh mesh, Matrix4f transform, GLTexture texture);
	public void queue(GLMesh mesh, Matrix4f transform, Matrix4f world_transform, String shader, GLTexture texture);

	public int width();
	public int height();

}
