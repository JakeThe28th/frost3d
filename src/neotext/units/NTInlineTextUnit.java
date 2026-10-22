package neotext.units;

import frost3d.implementations.SimpleCanvas;

public sealed interface NTInlineTextUnit permits NTFormattedText {//, NTMath {

	public static record NTPostRenderInfo (int next_character, int width, int height, boolean end_of_unit) {}
	
	/** Renders parts until the wrap_width is achieved. */
	public NTPostRenderInfo render(int start, SimpleCanvas canvas, int x, int y, int wrap_width);

	int part_count();

}
