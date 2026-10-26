package neotext;

import java.util.ArrayList;

import org.joml.Vector4f;

import frost3d.Framebuffer;
import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Rectangle;
import neotext.fonts.NTSimpleFontRenderer;
import neotext.units.NTFormattedText;
import neotext.units.NTInlineTextUnit;
import neotext.units.NTInlineTextUnit.NTPostRenderInfo;

public class NTEditor {
	
	private static final Vector4f WHITE = new Vector4f(1,1,1,1);

	ArrayList<NTInlineTextUnit> editor_contents = new ArrayList<>();
	
	{
		NTInlineTextUnit[] units = new NTInlineTextUnit[] {
				new NTFormattedText("Hello everyone! ", new NTSimpleFontRenderer()),
				new NTFormattedText("I'm Boldy. ", new NTSimpleFontRenderer()).bold(true),
				new NTFormattedText("Boldy the boldtype. ", new NTSimpleFontRenderer()).highlight_color(new Vector4f(0.5f,0.5f,0,1)).font_size(18),
				new NTFormattedText("Blue\n\n", new NTSimpleFontRenderer()).color(new Vector4f(0,0,1,1)).font_size(26),
				new NTFormattedText(" (There's an anime review.)\r\n"
									+ "* (Read it?)\r\n"
									+ "* Teen'z Corner\r\n"
									+ "  Monthly Toon Review:\r\n"
									+ "  Mew Mew Kissy Cutie 2\r\n"
									+ "* This reviewer had Mew Mew 2 as her first exposure to the series...\r\n"
									+ "* And let her tell you, it makes Mew Mew 1 look like a dumpster with sparkly cat ears!\r\n"
									+ "* With a darker storyline and more mature themes...\r\n"
									+ "* The second one treats the viewer like a real adult...\r\n"
									+ "* Instead of like an animal that will die if it goes ten seconds without seeing a beach ball.\r\n"
									+ "* Not to mention, Mew Mew's character in the first one...\r\n"
									+ "* Is more stale than the ramen I eat at home by myself with the lights off.\r\n"
									+ "* Teens and older should check out this dark masterpiece!\r\n"
									+ "* Signed,\r\n"
									+ "  The Anonymous Yellow Lizard\r\n"
									+ "* (You decide not to read it.)", new NTSimpleFontRenderer()).font_size(14),
		};
		for (NTInlineTextUnit unit : units) {
			editor_contents.add(unit);
		}
	}
	
	int cursor_position = 0;
	int selection_start = 0;
	int selection_end   = 0;
	
	
	boolean should_rerender_frame = true;
	SimpleCanvas editor_canvas;
	
	public NTEditor() {
		editor_canvas = new SimpleCanvas();
		editor_canvas.adopt(new Framebuffer(400, 400));
	}
	
	public void tickEditor() {
		
	}
	
	public void drawEditor(Rectangle area, SimpleCanvas canvas) {
		
		if (area.width() != editor_canvas.width() || area.height() != editor_canvas.height()) {
			Framebuffer old = editor_canvas.framebuffer();
			editor_canvas.adopt(new Framebuffer(area.width(), area.height()));
			editor_canvas.clear_color(1, 1, 1, 0);
			old.free();
		}
		
		if (should_rerender_frame) {
			
			int xx = 0;
			int yy = 0;
			int max_height = 0;
			for (NTInlineTextUnit unit : editor_contents) {
				NTPostRenderInfo info = new NTPostRenderInfo(0,0,0,false,false);
				int unit_start = 0;
				while (!info.end_of_unit()) {
					info = unit.render(unit_start, editor_canvas, xx, yy, area.width()-xx, area.width());
					xx += info.width();
					if (info.height() > max_height) max_height = info.height();
					if (info.newline() || (info.width() == 0 && !info.end_of_unit()) || (xx > area.width())) {
						xx = 0;
						yy += max_height;
					}
					unit_start = info.next_character();
					//Log.send(text_area.right()-xx + ", " + info.toString() + ", " + ((NTFormattedText) unit).contents );
				}
			}
			
			editor_canvas.draw_frame();
		}
		
		canvas.color(WHITE);
		canvas.rect(area.flipY(), 0, editor_canvas.framebuffer().texture());
		
	}

}
