package neotext.layout;

import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import frost3d.Input;
import frost3d.implementations.SimpleCanvas;
import frost3d.utility.Log;
import frost3d.utility.Rectangle;
import neotext.text.NTTextUnit;
import neotext.layout.NTDocument.CharacterInfo;

public class NTEditor {
	
	public NTDocument document;
	
	public NTEditor() {
		document = new NTDocument();
	}
	
	private static final Vector4f CURSOR_COLOR 			= new Vector4f(0,0,0,1);
	private static final Vector4f CURSOR_COLOR_INSERT 	= new Vector4f(0.2f, .8f, .8f, 0.75f);
	private static final Vector4f SELECTION_COLOR 		= new Vector4f(0.2f, .8f, .8f, 0.5f);
	
	int cursor_for_y_navigation = 0;
	int cursor = 0;
	int select_start = 0;
	int select_end   = 0;
	
	boolean insert = false;

	CharacterInfo cursor_draw;
	
	/** Renders the editor elements (selection, character cursor). */
	public void render(Rectangle area, SimpleCanvas canvas) {
		
		for (CharacterInfo info : document.rendered_characters) {
			
			// Selection
			if (info.real_char() >= select_start() && info.real_char() < select_end()) {
				document.document_canvas.color(SELECTION_COLOR);
				document.document_canvas.rect(info.area(), 0);
			}
			
			// Cursor
			if (info.real_char() == cursor && blink()) {
				if (insert) {
					document.document_canvas.color(CURSOR_COLOR_INSERT);
					document.document_canvas.rect(info.area(), 0);
				} else {
					document.document_canvas.color(CURSOR_COLOR);
					document.document_canvas.rect(info.area().left(), info.area().top(), info.area().left() + 2, info.area().bottom(), 0);
				}
			}
			
			document.document_canvas.draw_frame(false);
			
		}
					
	}	
	
	// --- Text Editing --- //
	
	public void tick(Input input, Vector2i mp) {
		
		int len = content_length();
		
		boolean shift = input.keyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
		
		if (input(input, GLFW.GLFW_KEY_LEFT  ) && cursor > 0   ) setcursor(cursor - 1, shift, false);
		if (input(input, GLFW.GLFW_KEY_RIGHT ) && cursor < len ) setcursor(cursor + 1, shift, false);
		
		if (input(input, GLFW.GLFW_KEY_HOME )) setcursor(0, shift, false); // TODO : home to last tab /or/ newline
		if (input(input, GLFW.GLFW_KEY_END  )) setcursor(len-1, shift, false); // TODO : end to before next newline

//		if (input(input, GLFW.GLFW_KEY_DOWN ) && cursor < len ) {
//			
//			// TODO : Make sure cursor and destination are on-screen
//			
//			for (CharacterInfo _cursor : document.rendered_characters) {
//				if (_cursor.real_char() == cursor) {
//					
//					CharacterInfo closest = null;
//					double closest_dist = -1;
//					for (CharacterInfo info : document.rendered_characters) {
//						Vector2i c = info.area().center();
//						Vector2i c2 = _cursor.area().center();
//				        double distance = Math.sqrt((Math.pow(c2.x - c.x, 2)) + (Math.pow(c2.y - c.y, 2)));
//				        if (info.area().top() > _cursor.area().top() && (closest_dist < 0 || distance < closest_dist)) {
//				        	closest_dist = distance;
//				        	closest = info;
//				        }
//					}
//					
//					setcursor(closest.real_char(), shift, true);
//					
//				}	
//			}
//		}

		if (input(input, GLFW.GLFW_KEY_ENTER)) {
			if (select_end() - select_start() > 0) {
				delete_selection();
			}
			insert(cursor, "\n");
			cursor += 1;
		}
		
		if (input(input, GLFW.GLFW_KEY_TAB)) {
			if (select_end() - select_start() > 0) {
				delete_selection();
			}
			insert(cursor, "\t");
			cursor += 1;
		}
		
		if (input(input, GLFW.GLFW_KEY_INSERT)) insert = !insert;
		
		if (input(input, GLFW.GLFW_KEY_DELETE) && len > 0) {
			if (select_end() - select_start() > 0) {
				delete_selection();
			} else {
				remove(cursor);
			}
		}
		
		if (input(input, GLFW.GLFW_KEY_BACKSPACE) && len > 0) {
			if (select_end() - select_start() > 0) {
				delete_selection();
			} else {
				remove(cursor-1);
			}
		}
		
		if (input.input_string() != "") {
			if (select_end() - select_start() > 0) {
				delete_selection();
			}
			insert(cursor, input.input_string());
			cursor += input.input_string().length();
			input.input_string("");
		}
		
		if (input.mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT) || input.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			CharacterInfo closest = null;
			double closest_dist = -1;
			for (CharacterInfo info : document.rendered_characters) {
				Vector2i c = info.area().center();
		        double distance = Math.sqrt((Math.pow(mp.x - c.x, 2)) + (Math.pow(mp.y - c.y, 2)));
		        if (closest_dist < 0 || distance < closest_dist) {
		        	closest_dist = distance;
		        	closest = info;
		        }
			}
			
			// Drag cursor & select
			setcursor(closest.real_char(), true, false); 

			// Reset slection
			if (input.mouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
				select_start = cursor;
				select_end   = cursor;
			}
			
		}
		
		if (input.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && input(input, GLFW.GLFW_KEY_Z)) {
			// TODO 
		}
		
		if (input.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && input(input, GLFW.GLFW_KEY_Y)) {
			// TODO 
		}
		
		if (input.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && input(input, GLFW.GLFW_KEY_C)) {
			Input.setClipboardString(selected_string_plaintext());
		}
		
		if (input.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && input(input, GLFW.GLFW_KEY_X)) {
			Input.setClipboardString(selected_string_plaintext());
			delete_selection();
		}
		
		if (input.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && input(input, GLFW.GLFW_KEY_V)) {
			input.input_string(input.input_string() + Input.getClipboardString());
		}
		
		if (input.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && input(input, GLFW.GLFW_KEY_A)) {
			select_start = 0;
			select_end = len;
			setcursor(len, shift, false);
		}
		
	}
	
	private String selected_string_plaintext() {
		String str = "";
		int index = 0;
		for (NTTextUnit unit : document.document) {
			int ulen = unit.character_count();
			if (index < select_end() && index + ulen > select_start()) {
				int internal_start = select_start() - index;
				int internal_end = select_end() - index;
				if (internal_end > ulen) internal_end = ulen;
				if (internal_start < 0) internal_start = 0;
				str += unit.contents.substring(internal_start, internal_end);
			}
			index += ulen;
		}
		return str;
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
		for (NTTextUnit unit : document.document) len += unit.character_count();
		return len;
	}
	
	// TODO cursor blinking [copied from GUITextbox ]
	boolean blink() { return (System.currentTimeMillis() - last_input_time < 500) || ((System.nanoTime() / 1000000000) % 2 == 0); }
	
	record CharacterLocation(NTTextUnit unit, int index) {}

	private CharacterLocation find_char(int index) {
		int current = 0;
		for (NTTextUnit unit : document.document) {
			if (index >= current && index < current + unit.character_count()) {
				return new CharacterLocation(unit, index - current);
			}
			current += unit.character_count();
		}
		return null;
	}
	
	// -- Actions -- //

	private void delete_selection() {
		int selection_length = select_end() - select_start();
		int selection_start = select_start();
		for (int i = 0; i < selection_length; i++) remove(selection_start);
	}
	
	private void insert(int index, String string) {
		CharacterLocation cursor_pos = find_char(index);
		cursor_pos.unit.insert(string, cursor_pos.index);
		if (insert) remove(cursor + 1);
	}
	
	private void remove(int index) {
		if (cursor >= index) {
			setcursor(cursor - 1, false, false);
		}
		if (index < 0) return;
		CharacterLocation cursor_pos = find_char(index);
		cursor_pos.unit.remove(cursor_pos.index);
		if (cursor_pos.unit.character_count() == 0) {
			document.document.remove(cursor_pos.unit);
		}
	}

	private void setcursor(int position, boolean shift, boolean y_nav) {
		setinputtime();
		cursor = position;
		if (!y_nav) cursor_for_y_navigation = position;
		if (cursor < 0) cursor = 0;
		int len = content_length();	if (cursor > len) cursor = len;
		if (!shift) select_start = cursor;
		select_end = cursor;
	}

}
