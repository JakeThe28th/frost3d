package neotext.units;

import java.util.ArrayList;

import org.joml.Vector4f;

import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Log;
import frost3d.utility.Rectangle;
import neotext.NTEditor.CharacterInfo;
import neotext.fonts.NTFontRenderer;
import neotext.fonts.NTSimpleFontRenderer;

public final class NTFormattedText implements NTInlineTextUnit {
	
	public static final boolean WRAP_ON_BREAKPOINTS = true;
	public static final boolean FORCE_WRAP_ON_EDGE = true;
	public static final boolean DEBUG_SHOW_WORD_BOUNDS = false;
	public static final boolean SKIP_SPACE_IF_FIRST_CHAR = true;

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
	public NTPostRenderInfo render(int start, SimpleCanvas canvas, int x, int y, int c_wrap_w, int t_wrap_w) {
		int p = start;
		int px = 0;
		int height = font_size;
		boolean newline = false;
		ArrayList<CharacterInfo> infos = new ArrayList<>();
		canvas.color(color);
		//Log.send((px < wrap_width && p < part_count()) + "?");
		
		if (SKIP_SPACE_IF_FIRST_CHAR) {
			if (p != 0 && !Character.isWhitespace(contents.charAt(p-1)))
			if (contents.charAt(p) == ' ') p++;
		}
		
		d: while (p < part_count()) {
			
			// get width until next break point
			int word_width = 0;
			int next_break_char  = p;
			while (next_break_char < part_count()) {
				if (is_word_boundary(contents.charAt(next_break_char))) break;
				word_width += kerning(p);
				next_break_char  ++;
			}
			
			// break the whole word if it's longer than the current remaining width, 
			// but not if it's longer than the entire width (in that case, break per-character)
			if (WRAP_ON_BREAKPOINTS && (px + word_width > c_wrap_w) && (word_width < t_wrap_w)) break d;
			
			// actually render section
			
			if (DEBUG_SHOW_WORD_BOUNDS) {
				
				canvas.color(new Vector4f(1,0,1,0.8f));
				canvas.rect(x + px, y, x + px + word_width, y + 10, 0);
				
				canvas.color(new Vector4f(0.1f,0,0,0.8f));
				if ((t_wrap_w-(px + word_width)) < 0) {
					canvas.color(new Vector4f(0.6f,0,0,0.8f));
				}
				canvas.textrenderer().font_size(10);
				canvas.text(x + px, y - 10, 0, "["+(t_wrap_w-(px + word_width))+"]");
				
				canvas.color(color);
			
			}

			while (p <= next_break_char && p < part_count()) {
				
				if (contents.charAt(p) == '\n') {
					infos.add(new CharacterInfo(new Rectangle(x+px, y, x+px+5, y+height), this, p));
					p++;
					newline = true;
					break d;
				}
				
				int k = kerning(p);
				
				// return from function when over edge (to force a character wrap)
				boolean break_later = false;
				if (FORCE_WRAP_ON_EDGE && px + k > c_wrap_w) {
					if (k > t_wrap_w) {
						break_later = true;
					} else {
						break d;
					}
				}
				
				if (bold) font.render_character(contents.charAt(p), canvas, x + px + 1, y, 0);
				font.render_character(contents.charAt(p), canvas, x + px, y, 0);
				
				infos.add(new CharacterInfo(new Rectangle(x+px, y, x+px+k, y+height), this, p));
				
				px += k;
				p  ++;
				
				if (break_later) break d;

			}
			
		}
		return new NTPostRenderInfo(p, px, height, p == part_count(), newline, infos);
	}

	public boolean is_word_boundary(char c) {
		return switch (c) {
			case ' ': yield true;
			case '-': yield true;
			case '~': yield true;
			case '+': yield true;
			default: yield false;
		};
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
	public void insert(String string, int position) {
		contents = contents.substring(0, position) + string + contents.substring(position, contents.length());
	}

}
