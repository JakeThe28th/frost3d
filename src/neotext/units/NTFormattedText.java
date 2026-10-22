package neotext.units;

import org.joml.Vector4f;

import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Log;
import frost3d.utility.Rectangle;
import neotext.fonts.NTFontRenderer;
import neotext.fonts.NTSimpleFontRenderer;

public final class NTFormattedText implements NTInlineTextUnit {
	
	public static final Vector4f BLACK = new Vector4f(0,0,0,1);
	
	NTFontRenderer  font = null;

	boolean 	italic 			= false;
	boolean 	bold 			= false;
	int 		font_size 		= 24;
	
	boolean 	strikethrough 	= false;
	boolean 	underlined 		= false;
	Vector4f 	highlight_color = null;
	Vector4f 	color 			= BLACK;
	
	public String contents = "";
	
	public NTFormattedText(String contents) {
		set_contents(contents);
	}
	
	public NTFormattedText(String contents, NTSimpleFontRenderer renderer) {
		this(contents);
		set_font(renderer);
	}

	private void set_contents(String contents) {
		this.contents = contents;
	}
	
	public NTFormattedText bold				(boolean 	v) { bold 				= v; return this; }
	public NTFormattedText italic			(boolean 	v) { italic 			= v; return this; }
	public NTFormattedText strikethrough	(boolean 	v) { strikethrough 		= v; return this; }
	public NTFormattedText underlined		(boolean 	v) { underlined 		= v; return this; }
	public NTFormattedText color			(Vector4f 	v) { color 				= v; return this; }
	public NTFormattedText highlight_color	(Vector4f 	v) { highlight_color 	= v; return this; }
	
	public NTFormattedText font_size		(int	 	v) { font_size 	= v; font.font_size(v); return this; }

	/** Note: the font object passed here is retained */
	public void set_font(NTFontRenderer font) {
		font.bold		(bold);
		font.italic		(italic);
		font.font_size	(font_size);
		this.font 		= font;
	}

	@Override
	public NTPostRenderInfo render(int start, SimpleCanvas canvas, int x, int y, int wrap_width) {
		int p = start;
		int px = 0;
		int height = font_size;
		canvas.color(color);
		//Log.send((px < wrap_width && p < part_count()) + "?");
		while (p < part_count()) {
			int k = kerning(p);
						
			if (px + k > wrap_width) break;
			
			if (bold) {
				font.render_character(contents.charAt(p), canvas, x + px + 1, y, 0);
			}
			font.render_character(contents.charAt(p), canvas, x + px, y, 0);
			px += k;
			p  ++;
			//Log.send((px < wrap_width && p < part_count()) + "? , " + p);
		}
		return new NTPostRenderInfo(p, px, height, p == part_count());
	}

	private int kerning(int p) {
		if (part_count() == 1) return 0;
		if (p == 0) {
			return font.kerning(' ', contents.charAt(p), contents.charAt(p+1));
		} if (p != part_count()-1) {
			return font.kerning(contents.charAt(p-1), contents.charAt(p), contents.charAt(p+1));
		} else {
			return font.kerning(contents.charAt(p-1), contents.charAt(p), ' ');
		}
	}

	@Override
	public int part_count() {
		return contents.length();
	}

}
