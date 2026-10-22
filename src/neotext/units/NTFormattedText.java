package neotext.units;

import org.joml.Vector4f;

import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Rectangle;
import neotext.fonts.NTFontRenderer;

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
	
	int[] x_offsets = new int[0];
	String contents = "";

	private boolean should_recalculate_x_offsets;
	
	public NTFormattedText(String contents) {
		set_contents(contents);
	}
	
	private void set_contents(String contents) {
		this.contents = contents;
		should_recalculate_x_offsets = true;
	}
	
	private void recalculate_x_offsets() {
		this.x_offsets = new int[contents.length()];
		int px = 0;
		for (int p = 0; p < part_count(); p++) {
			x_offsets[p] = px;
			px += kerning(p);
		}
	}

	/** Note: the font object passed here is retained */
	public void set_font(NTFontRenderer font) {
		font.bold		(bold);
		font.italic		(italic);
		font.font_size	(font_size);
		this.font 		= font;
	}

	@Override
	public Rectangle size() {
		return size(0, part_count());
	}

	@Override
	public void render(SimpleCanvas canvas, int x, int y) {
		render(0, part_count(), canvas, x, y);
	}

	@Override
	public Rectangle size(int start, int end) {
		if (should_recalculate_x_offsets) recalculate_x_offsets();
		return new Rectangle(0,0,x_offsets[end]-x_offsets[start],font_size);
	}

	@Override
	public void render(int start, int end, SimpleCanvas canvas, int x, int y) {
		int px = x;
		canvas.color(color);
		for (int p = start; p < end; p++) {
			
			font.render_character(contents.charAt(p), canvas, px, y, 0);
			
			px += kerning(p);
			
		}
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

	@Override
	public int part_x_offset(int start) {
		if (should_recalculate_x_offsets) recalculate_x_offsets();
		return x_offsets[start];
	}

}
