package neotext.layout;

import java.util.ArrayList;

import org.joml.Vector4f;

import frost3d.Framebuffer;
import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Rectangle;
import neotext.text.NTSimpleFontRenderer;
import neotext.text.NTTextUnit;
import neotext.text.NTTextUnit.NTPostRenderInfo;

public class NTDocument {
	
	private static final Vector4f WHITE = new Vector4f(1,1,1,1);
	
	public record CharacterInfo(Rectangle area, NTTextUnit source, int source_char, int real_char) {}
	
	// --= Persistent State =-- //
	ArrayList<NTTextUnit> 	document = new ArrayList<>(); // TODO : allow other types like images here
	SimpleCanvas 			document_canvas;
	
	int						tab_size_px = 40;
	
	// --= Temporary State =-- //
	ArrayList<CharacterInfo> rendered_characters = new ArrayList<>();
	boolean 				 should_rerender_frame = true;
	
	public NTDocument() {
		document_canvas = new SimpleCanvas();
		document_canvas.adopt(new Framebuffer(400, 400));
	}
	
	{
		NTTextUnit[] units = new NTTextUnit[] {
			new NTTextUnit("Hello everyone! ", new NTSimpleFontRenderer()),
			new NTTextUnit("I'm Boldy. ", new NTSimpleFontRenderer()).bold(true),
			new NTTextUnit("Boldy the boldtype. ", new NTSimpleFontRenderer()).highlight_color(new Vector4f(0.5f,0.5f,0,1)).font_size(18),
			new NTTextUnit("Blue\n\n", new NTSimpleFontRenderer()).color(new Vector4f(0,0,1,1)).font_size(26),
			new NTTextUnit(" (There's an anime review.)\r\n"
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
		for (NTTextUnit unit : units) document.add(unit);
	}
	
	/** Renders the provided Content. */
	public void render(Rectangle area, SimpleCanvas canvas) {
				
		if (area.width() != document_canvas.width() || area.height() != document_canvas.height()) {
			Framebuffer old = document_canvas.framebuffer();
			document_canvas.adopt(new Framebuffer(area.width(), area.height()));
			document_canvas.clear_color(1, 1, 1, 0);
			old.free();
		}
		
		if (should_rerender_frame) {
						
			rendered_characters.clear();
			int current_chara = 0;
			
			int xx = 0;
			int yy = 0;
			int max_height = 0;
			for (NTTextUnit unit : document) {
				NTPostRenderInfo info = new NTPostRenderInfo(0,0,0,false,false,null);
				int unit_start = 0;
				while (!info.end_of_unit()) {
					info = unit.render(current_chara, unit_start, document_canvas, xx, yy, area.width()-xx, area.width(), tab_size_px);
					xx += info.width();
					if (info.height() > max_height) max_height = info.height();
					if (info.newline() || (info.width() == 0 && !info.end_of_unit()) || (xx > area.width())) {
						xx = 0;
						yy += max_height;
					}
					unit_start = info.next_character();
				
					// TODO : handle offscreen for scrolling
					// i'll probably skip the loop body when i implement that,
					// so, i'm putting this line in here...
					rendered_characters.addAll(info.infos());
					
				}
				current_chara += unit.character_count();
			}
			
			
			document_canvas.draw_frame();
		}
		
		canvas.color(WHITE);
		canvas.rect(area.flipY(), 0, document_canvas.framebuffer().texture());
		
	}

}
