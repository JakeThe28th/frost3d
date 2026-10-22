package neotext.units;

import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Rectangle;

public sealed interface NTInlineTextUnit permits NTFormattedText {//, NTMath {
	
	public Rectangle 	size();	
	public void 		render(SimpleCanvas canvas, int x, int y);
	
	public Rectangle 	size(int start, int end);	
	public void 		render(int start, int end, SimpleCanvas canvas, int x, int y);
	
	public int 			part_count();
	public int 			part_x_offset(int start);


}
