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
import neotext.NTEditor;
import neotext.fonts.NTSimpleFontRenderer;
import neotext.units.NTFormattedText;
import neotext.units.NTInlineTextUnit;
import neotext.units.NTInlineTextUnit.NTPostRenderInfo;

public class DEMO_NeoTextRenderingEditing {

	public static void main(String[] args) {

		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(512, 512, "DEMO_NeoTextRendering");
		BuiltinShaders.init();
		SimpleCanvas canvas = new SimpleCanvas();
		
		canvas.textrenderer(new SimpleTextRenderer());
				
		NTEditor text_editor = new NTEditor();
		
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
			
			text_editor.tickEditor(window.input());
			text_editor.drawEditor(text_area, canvas);
			
			canvas.color(NTFormattedText.BLACK);
			canvas.text(10, 10, 0, String.format("%.2f", (((System.nanoTime() - frame_start) / 10000) / 100d), args));
			frame_start = System.nanoTime();
			
			canvas.draw_frame();
			window.tick();
		}

	}

}
