package neotext.fonts;

import frost3d.implementations.SimpleTextRenderer;
import frost3d.interfaces.F3DCanvas;

public class NTSimpleFontRenderer implements NTFontRenderer {
	
	SimpleTextRenderer renderer = new SimpleTextRenderer();
	
	public boolean 	italic 			= false;
	public boolean 	bold 			= false;
	public int 		font_size 		= 12;
	
	@Override public void bold(boolean bold) 		{ this.bold = bold;}
	@Override public void italic(boolean italic) 	{ this.italic = italic; }
	@Override public void font_size(int font_size) 	{ this.font_size = font_size; renderer.font_size(font_size); }
	
	@Override
	public int kerning(char prev, char current, char next) {
		return renderer.advance(String.valueOf(current), 0);
	}
	
	@Override
	public void render_character(char character, F3DCanvas canvas, int x, int y, int z) {
		renderer.character(canvas, x, y, z, character);
		if (bold) renderer.character(canvas, x + 1, y, z, character);
	}

}
