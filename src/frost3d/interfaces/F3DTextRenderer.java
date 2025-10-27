package frost3d.interfaces;

import org.joml.Vector2i;

public interface F3DTextRenderer {
	
	public void font_size(int new_size);
	
	public Vector2i size(String text);
	
	/** Draw one line of text, where 0, 0 is the 'top left' of the character*<br>
	 *  *Not exactly the top left, but close enough... */
	public void text(F3DCanvas canvas, int x, int y, int z, String text);
	
}
