package neotext.units;

import java.util.List;

import frost3d.implementations.SimpleCanvas;
import neotext.NTEditor.CharacterInfo;

public sealed interface NTInlineTextUnit permits NTFormattedText {//, NTMath {

	public static record NTPostRenderInfo (int next_character, int width, int height, boolean end_of_unit, boolean newline, List<CharacterInfo> infos) {}
	
	/** Renders parts until the wrap_width is achieved. */
	public NTPostRenderInfo render(int start, SimpleCanvas canvas, int x, int y, int current_wrap_width, int total_wrap_width);

	int part_count();

	public void insert(String string, int position);

}
