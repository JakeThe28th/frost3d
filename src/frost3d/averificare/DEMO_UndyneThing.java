package frost3d.averificare;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleMesh;
import frost3d.implementations.SimpleTextRenderer;
import frost3d.implementations.SimpleWindow;
import frost3d.interfaces.GLTexture;

public class DEMO_UndyneThing {
	
	static SimpleMesh unit_square;
	
	public static void init() {
		unit_square = new SimpleMesh(
				new float[] {
				1,  1, 0.0f,  // top right
				1,  0, 0.0f,  // bottom right
			    0,  0, 0.0f,  // bottom left
			    0,  1, 0.0f   // top left 
				},
				new float[] {
				1,1,
				1,0,
				0,0,
				0,1},
				new int[] {
				    0, 1, 3,   // first triangles
				    1, 2, 3    // second triangle
				});
	}
	
	public static void main(String[] args) {
		
		// ............... //
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(500, 500, "");
		BuiltinShaders.init();
		SimpleCanvas canvas = new SimpleCanvas();
		canvas.size(window.width, window.height);
		canvas.textrenderer(new SimpleTextRenderer());
		canvas.textrenderer().font_size(30);
		// ............... //
		
		init();
				
		canvas.color(new Vector4f(1,1,1,1));
		
		float rotation = 0;
		
		while (!window.should_close()) {
			
			Vector2i vec = new Vector2i(0);
			
			if (window.input().keyDown(GLFW.GLFW_KEY_W)) vec.y = -1; // up
			if (window.input().keyDown(GLFW.GLFW_KEY_A)) vec.x = -1; // left
			if (window.input().keyDown(GLFW.GLFW_KEY_S)) vec.y =  1; // down
			if (window.input().keyDown(GLFW.GLFW_KEY_D)) vec.x =  1; // right

			if (vec.x != 0 || vec.y != 0) {
				float rotation_y = (vec.y == 1 ? 90 : 270);
				float rotation_x = (vec.x == 1 ? 0  : 180);
				
				// if there's no rotation in one of the directions, then don't average them
				if (vec.y == 0) rotation = rotation_x;
				if (vec.x == 0) rotation = rotation_y;
				
				// averaging between 0 and 270 should wrap around, so replace 0 with 360
				if (rotation_x == 0 && rotation_y == 270) rotation_x = 360;
				
				// average the rotations
				if (vec.y != 0 && vec.x != 0) rotation = ( rotation_x + rotation_y ) / 2;
				
			}
			
			Matrix4f matrix = new Matrix4f();
			matrix.translate(window.width/2, window.height/2, 0);
			matrix.scale(100, 100, 0);
			matrix.rotateAffineXYZ(0, 0, (float) Math.toRadians(rotation));
			matrix.translate(.5f, -0.5f, 0);

			canvas.color(window.input().keyDown(GLFW.GLFW_KEY_W) ? new Vector4f(1,.5f,.9f,1) : new Vector4f(0,0,0,1));
			canvas.text(10, 10, 0, "W");
			canvas.color(window.input().keyDown(GLFW.GLFW_KEY_A) ? new Vector4f(1,.5f,.9f,1) : new Vector4f(0,0,0,1));
			canvas.text(30, 10, 0, "A");
			canvas.color(window.input().keyDown(GLFW.GLFW_KEY_S) ? new Vector4f(1,.5f,.9f,1) : new Vector4f(0,0,0,1));
			canvas.text(50, 10, 0, "S");
			canvas.color(window.input().keyDown(GLFW.GLFW_KEY_D) ? new Vector4f(1,.5f,.9f,1) : new Vector4f(0,0,0,1));
			canvas.text(70, 10, 0, "D");

			canvas.color(new Vector4f(1,1,1,1));
			canvas.queue(unit_square, matrix, (GLTexture) null);
			
			canvas.draw_frame();
			window.tick();
		}
		
	}

}
