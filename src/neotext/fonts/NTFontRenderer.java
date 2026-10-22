package neotext.fonts;

import frost3d.interfaces.F3DCanvas;

public interface NTFontRenderer {
	
	public void bold(boolean bold);
	public void italic(boolean italic);
	public void font_size(int font_size);
	
	public int kerning(char prev, char current, char next);
	public void render_character(char character, F3DCanvas canvas, int x, int y, int z);

}
