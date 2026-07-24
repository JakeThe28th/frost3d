package frost3d.averificare;

import org.joml.Vector2f;
import org.joml.Vector4f;

import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleWindow;
import frost3d.utility.Utility;

public class DEMO_LerpThingy {
	
	static final Vector4f BLACK = new Vector4f(0,0,0,1);
	static final Vector4f RED = new Vector4f(1,0,0,1);

	public static void main(String[] args) { 
		
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(500, 500, "");
		BuiltinShaders.init();
		SimpleCanvas canvas = new SimpleCanvas();
		
		Vector2f pos0 = new Vector2f();
		Vector2f pos1 = new Vector2f();
		Vector2f pos2 = new Vector2f();

		while (!window.should_close()) {
			canvas.size(window.width, window.height);
			
			float lerp_amount = 0.15f;
			
			pos0.x = (float) Utility.lerp(pos0.x, window.input().mouseX(), lerp_amount);
			pos0.y = (float) Utility.lerp(pos0.y, window.input().mouseY(), lerp_amount);
			
			pos1.x = (float) Utility.lerp(pos1.x, pos0.x, lerp_amount);
			pos1.y = (float) Utility.lerp(pos1.y, pos0.y, lerp_amount);
			
			pos2.x = (float) Utility.lerp(pos2.x, pos1.x, lerp_amount);
			pos2.y = (float) Utility.lerp(pos2.y, pos1.y, lerp_amount);
			
			canvas.color(RED);
			canvas.dot(window.input().mouseX(), window.input().mouseY(), 0, 10);
			
			canvas.color(BLACK);
			canvas.dot((int) pos0.x, (int) pos0.y, 0, 20);
			
			canvas.color(BLACK);
			canvas.dot((int) pos1.x, (int) pos1.y, 0, 20);
			
			canvas.color(BLACK);
			canvas.dot((int) pos2.x, (int) pos2.y, 0, 20);
			

			canvas.draw_frame();
			window.tick();
		}
		
		
	}

}
