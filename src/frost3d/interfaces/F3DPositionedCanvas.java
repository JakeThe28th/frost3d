package frost3d.interfaces;

import org.joml.Vector2i;

import frost3d.enums.Alignment;
import frost3d.enums.FillMode;

public interface F3DPositionedCanvas extends F3DCanvas {
	
	public void halign(Alignment a);
	public void valign(Alignment a);
	public void fillmode(FillMode f);
	public void outrectangle(int outer_width, int outer_height);
	
	/** A mesh of a rectangle of the normalized coordinates and texture of this canvas. */
	public GLMesh mesh();
	
	/** Converts a point from the space in which this canvas will
	 *  be drawn to the space within this canvas, in units. */
	public Vector2i internalpoint(Vector2i point);
	
	public void draw(F3DCanvas canvas);
	public void draw(F3DWindow window);
	

}
