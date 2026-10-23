package frost3d.averificare;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.enums.CursorType;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleTextRenderer;
import frost3d.implementations.SimpleWindow;
import frost3d.utility.Log;
import frost3d.utility.Rectangle;
import neotext.fonts.NTSimpleFontRenderer;
import neotext.units.NTFormattedText;
import neotext.units.NTInlineTextUnit;
import neotext.units.NTInlineTextUnit.NTPostRenderInfo;

public class DEMO_NeoTextRendering {

	public static void main(String[] args) {

		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(512, 512, "DEMO_NeoTextRendering");
		BuiltinShaders.init();
		SimpleCanvas canvas = new SimpleCanvas();
		
		canvas.textrenderer(new SimpleTextRenderer());
				
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
		
		Rectangle text_area = new Rectangle(30, 40, 450, 480);
		
		boolean dragging_left = false;
		boolean dragging_right = false;
		boolean dragging_top = false;
		boolean dragging_bottom = false;
		
		long frame_start = System.nanoTime();
		
		// window.setVsync(false);
		
		while (!window.should_close()) {
			
			canvas.size(window.width, window.height);
			
			
			if (!window.input().mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
				dragging_left = false;
				dragging_right = false;
				dragging_top = false;
				dragging_bottom = false;
			}
			
			       if (Math.round(window.input().mouseX() / 20f) == Math.round(text_area.left() / 20f)) {
			    window.input().cursor(CursorType.RESIZE_EW_CURSOR);
				if (window.input().mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
					dragging_left = true;
				}
			} else if (Math.round(window.input().mouseX() / 20f) == Math.round(text_area.right() / 20f)) {
				window.input().cursor(CursorType.RESIZE_EW_CURSOR);
				if (window.input().mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
					dragging_right = true;
				}
			} else if (Math.round(window.input().mouseY() / 20f) == Math.round(text_area.top() / 20f)) {
				window.input().cursor(CursorType.RESIZE_NS_CURSOR);
				if (window.input().mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
					dragging_top = true;
				}
			} else if (Math.round(window.input().mouseY() / 20f) == Math.round(text_area.bottom() / 20f)) {
				window.input().cursor(CursorType.RESIZE_NS_CURSOR);
				if (window.input().mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
					dragging_bottom = true;
				}
			} else {
				window.input().cursor(CursorType.ARROW_CURSOR);
			}
			
			if (dragging_left) {
				window.input().cursor(CursorType.RESIZE_EW_CURSOR);
				text_area = new Rectangle(window.input().mouseX(), text_area.top(), text_area.right(), text_area.bottom());
			}
			
			if (dragging_right) {
				window.input().cursor(CursorType.RESIZE_EW_CURSOR);
				text_area = new Rectangle(text_area.left(), text_area.top(), window.input().mouseX(), text_area.bottom());
			}
			
			if (dragging_top) {
				window.input().cursor(CursorType.RESIZE_NS_CURSOR);
				text_area = new Rectangle(text_area.left(), window.input().mouseY(), text_area.right(), text_area.bottom());
			}
			
			if (dragging_bottom) {
				window.input().cursor(CursorType.RESIZE_NS_CURSOR);
				text_area = new Rectangle(text_area.left(), text_area.top(), text_area.right(), window.input().mouseY());
			}

			canvas.color(new Vector4f(1f, 0.8f, 0.8f, 1f));
			canvas.rect(text_area, 0);
			
			int xx = text_area.left();
			int yy = text_area.top();
			int max_height = 0;
			for (NTInlineTextUnit unit : units) {
				NTPostRenderInfo info = new NTPostRenderInfo(0,0,0,false);
				int unit_start = 0;
				while (!info.end_of_unit()) {
					info = unit.render(unit_start, canvas, xx, yy, text_area.right()-xx, text_area.width());
					xx += info.width();
					if (info.height() > max_height) max_height = info.height();
					if ((info.width() == 0 && !info.end_of_unit()) || (xx > text_area.right())) {
						xx = text_area.left();
						yy += max_height;
					}
					unit_start = info.next_character();
					//Log.send(text_area.right()-xx + ", " + info.toString() + ", " + ((NTFormattedText) unit).contents );
				}
			}
			
			
			canvas.color(NTFormattedText.BLACK);
			canvas.text(10, 10, 0, String.format("%.2f", (((System.nanoTime() - frame_start) / 10000) / 100d), args));
			frame_start = System.nanoTime();
			
			canvas.draw_frame();
			window.tick();
		}

	}

}
