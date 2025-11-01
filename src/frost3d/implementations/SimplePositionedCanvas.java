package frost3d.implementations;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL40;

import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.enums.Alignment;
import frost3d.enums.FillMode;
import frost3d.interfaces.F3DCanvas;
import frost3d.interfaces.F3DPositionedCanvas;
import frost3d.interfaces.F3DWindow;
import frost3d.interfaces.GLMesh;
import frost3d.utility.FloatRectangle;
import frost3d.utility.Utility;

public class SimplePositionedCanvas extends SimpleCanvas implements F3DPositionedCanvas {

	Alignment halign = Alignment.MIDDLE;
	@Override public void halign(Alignment a) { halign = a; }
	
	Alignment valign = Alignment.MIDDLE;
	@Override public void valign(Alignment a) { valign = a; }
	
	FillMode fillmode = FillMode.CONTAIN;
	@Override public void fillmode(FillMode f) { fillmode = f; }
	
	int outer_width = -1;
	int outer_height = -1;
	@Override
	public void outrectangle(int outer_width, int outer_height) {
		this.outer_width = outer_width;
		this.outer_height = outer_height;
	}
	
	@Override
	public Vector2i internalpoint(Vector2i input) {
		if (outer_width == -1 || outer_height == -1) throw new RuntimeException("external rectangle not set");
		FloatRectangle rect = Utility.getFittedRectangle(outer_width, outer_height, width, height, halign, valign, fillmode);
		Vector2d vec = Utility.getNormalizedInternalCoordinates(rect, new Vector2d(input.x / (float) outer_width, input.y / (float) outer_height));
		return new Vector2i((int) (vec.x * width), (int) (vec.y * height));
	}

	@Override
	public GLMesh mesh() {
		if (outer_width == -1 || outer_height == -1) throw new RuntimeException("external rectangle not set");
		return Utility.mesh(Utility.getFittedRectangle(outer_width, outer_height, width, height, halign, valign, fillmode), 0);
	}

	@Override
	public void draw(F3DCanvas canvas) {
		canvas.queue(mesh(), new Matrix4f(), new Matrix4f().ortho(-1, 1, -1, 1, -1, 1), BuiltinShaders.SCREEN, framebuffer().texture());
	}

	@Override
	public void draw(F3DWindow window) {
		window.bind();
		GLState.clear();
		BuiltinShaders.SCREEN.bind();		
		mesh().bind();
		GL40.glActiveTexture(GL40.GL_TEXTURE0);									// Activate texture0
		GL40.glBindTexture(GL40.GL_TEXTURE_2D,  framebuffer().texture().gltexture());	// Bind the texture t to texture0
		
		GL40.glDrawElements(GL_TRIANGLES, mesh().index_count(), GL_UNSIGNED_INT, 0);
	}

}
