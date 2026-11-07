package neotext;

import java.util.ArrayList;

import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import frost3d.Framebuffer;
import frost3d.Input;
import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Rectangle;
import frost3d.utility.Utility;
import neotext.fonts.NTSimpleFontRenderer;
import neotext.units.NTFormattedText;
import neotext.units.NTInlineTextUnit;
import neotext.units.NTInlineTextUnit.NTPostRenderInfo;

public class NTEditor {
	
	private static final Vector4f WHITE = new Vector4f(1,1,1,1);

	private static final Vector4f CURSOR_COLOR = new Vector4f(Utility.fromHex("#0031f5"));
	
	static { CURSOR_COLOR.w = 0.7f; }

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
	
	int cursor = 0;
	int select_start = 0;
	int select_end   = 0;

	CharacterInfo cursor_draw;
	
	public record CharacterInfo(Rectangle area, NTInlineTextUnit source, int source_char) {}
	
	ArrayList<CharacterInfo> on_screen_character_info = new ArrayList<CharacterInfo>();
	
	boolean should_rerender_frame = true;
	SimpleCanvas editor_canvas;
	
	public NTEditor() {
		editor_canvas = new SimpleCanvas();
		editor_canvas.adopt(new Framebuffer(400, 400));
	}

	Rectangle last_draw_area = null;
	
	public void drawEditor(Rectangle area, SimpleCanvas canvas) {
		
		last_draw_area = area;
		
		if (area.width() != editor_canvas.width() || area.height() != editor_canvas.height()) {
			Framebuffer old = editor_canvas.framebuffer();
			editor_canvas.adopt(new Framebuffer(area.width(), area.height()));
			editor_canvas.clear_color(1, 1, 1, 0);
			old.free();
		}
		
		if (should_rerender_frame) {
			
			cursor_draw = null;
			
			on_screen_character_info.clear();
			int current_chara = 0;
			
			int xx = 0;
			int yy = 0;
			int max_height = 0;
			for (NTInlineTextUnit unit : editor_contents) {
				NTPostRenderInfo info = new NTPostRenderInfo(0,0,0,false,false,null);
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
				
					// TODO : handle offscreen for scrolling
					// i'll probably skip the loop body when i implement that,
					// so, i'm putting this line in here...
					on_screen_character_info.addAll(info.infos());
					
					// scuffed check but it's midnight lol
					if (cursor_draw == null && cursor >= current_chara && cursor < current_chara + unit_start) {
						for (CharacterInfo c_info : info.infos()) {
							if (current_chara + c_info.source_char == cursor) {
								cursor_draw = c_info;
							}
						}
					}
					
					// select box detection
					for (CharacterInfo c_info : info.infos()) {
						if (current_chara + c_info.source_char >= select_start() && current_chara + c_info.source_char < select_end()) {
							editor_canvas.color(new Vector4f(0,1,1,.5f));
							editor_canvas.rect(c_info.area, 0);
						}
					}
					
				}
				current_chara += unit.part_count();
			}
			
			if (cursor_draw != null) {
				editor_canvas.color(CURSOR_COLOR);
				editor_canvas.rect(cursor_draw.area, 0);
			}
			
			editor_canvas.draw_frame();
		}
		
		canvas.color(WHITE);
		canvas.rect(area.flipY(), 0, editor_canvas.framebuffer().texture());
		
	}
	
	// --- Text Editing --- //
	
	public void tickEditor(Input input) {
		
		int len = content_length();
		
		boolean shift = input.keyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
		
		if (input(input, GLFW.GLFW_KEY_LEFT) && cursor > 0) setcursor(cursor - 1, shift);
		if (input(input, GLFW.GLFW_KEY_RIGHT) && cursor < len) setcursor(cursor + 1, shift);
		
		if (input(input, GLFW.GLFW_KEY_HOME)) setcursor(0, shift); // TODO : home to last tab /or/ newline
		if (input(input, GLFW.GLFW_KEY_END)) setcursor(len-1, shift); // TODO : end to before next newline

		if (input(input, GLFW.GLFW_KEY_DELETE) && len > 0) {
			remove(cursor);
		}
		
		if (input(input, GLFW.GLFW_KEY_BACKSPACE) && len > 0) {
			remove(cursor-1);
		}
		
		if (input.input_string() != "") {
			insert(cursor, input.input_string());
			cursor += input.input_string().length();
			input.input_string("");
		}
		
		if (input.mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) || input.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			CharacterInfo closest = null;
			double closest_dist = -1;
			for (CharacterInfo info : on_screen_character_info) {
				Vector2i c = info.area.center().add(last_draw_area.left(), last_draw_area.top());
				int x_delta = input.mouseX() - c.x;
				int y_delta = input.mouseY() - c.y;
		        double distance = Math.sqrt((x_delta*x_delta) + (y_delta*y_delta));
		        if (closest_dist < 0 || distance < closest_dist) {
		        	closest_dist = distance;
		        	closest = info;
		        }
			}
			setcursor(start(closest.source) + closest.source_char, shift); 
		}
		
	}

	private boolean input(Input input, int key) {
		return input.keyPressed(key) || input.keyRepeated(key);
	}
	

	int select_end  () { return (select_end > select_start) ? select_end   : select_start; }
	int select_start() { return (select_end > select_start) ? select_start : select_end;   }
	
	long last_input_time = System.currentTimeMillis();
	void setinputtime() { last_input_time = System.currentTimeMillis(); }

	int content_length() { 
		int len = 0;
		for (NTInlineTextUnit unit : editor_contents) len += unit.part_count();
		return len;
	}
	
	// TODO cursor blinking [copied from GUITextbox ]
	boolean blink() { return (System.currentTimeMillis() - last_input_time < 500) || ((System.nanoTime() / 1000000000) % 2 == 0); }
	
	record CharacterLocation(NTInlineTextUnit unit, int index) {}

	private CharacterLocation find_char(int index) {
		int current = 0;
		for (NTInlineTextUnit unit : editor_contents) {
			if (index >= current && index < current + unit.part_count()) {
				return new CharacterLocation(unit, index - current);
			}
			current += unit.part_count();
		}
		return null;
	}
	
	private int start(NTInlineTextUnit source) {
		int current = 0;
		for (NTInlineTextUnit unit : editor_contents) {
			if (unit == source) {
				return current;
			}
			current += unit.part_count();
		}
		return -1;
	}
	
	// -- Actions -- //
	
	private void insert(int index, String string) {
		CharacterLocation cursor_pos = find_char(index);
		cursor_pos.unit.insert(string, cursor_pos.index);
	}
	
	private void remove(int index) {
		if (cursor >= index) {
			setcursor(cursor - 1, false);
		}
		if (index < 0) return;
		CharacterLocation cursor_pos = find_char(index);
		cursor_pos.unit.remove(cursor_pos.index);
		if (cursor_pos.unit.part_count() == 0) {
			editor_contents.remove(cursor_pos.unit);
		}
	}

	private void setcursor(int position, boolean shift) {
		setinputtime();
		cursor = position;
		if (cursor < 0) cursor = 0;
		int len = content_length();	if (cursor > len) cursor = len;
		if (!shift) select_start = cursor + 1;
		select_end = cursor + 1;
	}

}
