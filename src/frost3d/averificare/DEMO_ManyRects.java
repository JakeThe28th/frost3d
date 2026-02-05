package frost3d.averificare;

import java.util.Random;

import org.joml.Vector2i;
import org.joml.Vector4f;

import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleWindow;

public class DEMO_ManyRects {
	
	@SuppressWarnings("unused")
	private static final Vector4f BLACK = new Vector4f(0,0,0,1);
	
	static boolean display_tri = false;
	
	
	static int gridsize = 128;
	
	static Vector2i[] positions = new Vector2i [gridsize*gridsize];
	static Vector4f[] colors    = new Vector4f [gridsize*gridsize];
	
	static float   [] xrand		= new float	   [gridsize*gridsize];
	static float   [] yrand		= new float	   [gridsize*gridsize];

	public static void main(String[] args) {
		
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(1800, 1100, "Canvas Rendering Tests");
		BuiltinShaders.init();
		
		SimpleCanvas canvas = new SimpleCanvas();
		
		for (int i = 0; i < colors.length; i++) {
			float r = (float) Math.random();
			float g = (float) Math.random();
			float b = (float) Math.random();
			float sum = r + g + b;
			colors[i] = new Vector4f(r/sum, g/sum, b/sum, 1);					
		}
		
		int x_add = (int) ((float) window.width() / (float) gridsize);
		int y_add = (int) ((float) window.height() / (float) gridsize);

		for (int x = 0; x < gridsize; x++)
		for (int y = 0; y < gridsize; y++) {
			positions[(x*gridsize) + y] = new Vector2i((x_add/2) + (x*x_add), (y_add/2) + (y*y_add), 1);					
		}
		
		for (int i = 0; i < xrand.length; i++) {
			Vector2i p = positions[i];
			Random rand = new Random(p.x * 17 * p.y * 31 );
			float xr = rand.nextFloat();
			float yr = rand.nextFloat();
			xrand[i] = 	xr - 0.5f;
			yrand[i] = 	yr - 0.5f;
		}
		
		int s = 5;

		long st = System.currentTimeMillis();
		
		while (!window.should_close()) {
			
			float time = (System.currentTimeMillis() - st) / 200f;
			
			canvas.size(window.width, window.height);
			
			for (int i = 0; i < positions.length; i++) {
				canvas.color(colors[i]);
				Vector2i p = positions[i];
				p.add((int) (Math.sin(time + xrand[i])*5), (int) (Math.cos(time + yrand[i])*5));
				canvas.rect(p.x-s, p.y-s, p.x+s, p.y+s, 0);
			}
			
			canvas.draw_frame();
			window.tick();
			
			//System.gc();
		}
		
		window.end();
		GLState.endGLFW();
	}

}
