package neotext;

import neotext.fonts.NTFontRenderer;

/** Renders a block of text. */
public interface NTSectionRenderer {

	void enable_word_wrap();
	void disable_word_wrap();
	
	void set_font(NTFontRenderer font);
	void set_content(NTTextContent content);
	
	void render(F3DCanvas canvas, Rectangle bounding_box);
	
}
