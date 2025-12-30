package frost3d.averificare;

import org.joml.Vector4f;

import frost3d.GLState;
import frost3d.Shapes;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleWindow;
import frost3d.interfaces.F3DCanvas;

public class DEMO_Lines {
	
	@SuppressWarnings("unused")
	private static final Vector4f BLACK = new Vector4f(0,0,0,1);
	private static final Vector4f RED = new Vector4f(1,0,0,1);
	private static final Vector4f BLUE = new Vector4f(0,0,1,1);
	
	static boolean display_tri = false;

	public static void main(String[] args) {
		
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(600, 800, "Canvas Rendering Tests");
		BuiltinShaders.init();
		
		SimpleCanvas canvas = new SimpleCanvas();
		
		while (!window.should_close()) {
			
			canvas.size(window.width, window.height);
			
			//canvas.color(RED);
			//Shapes.line(canvas, 100, 100, window.input().mouseX(), window.input().mouseY(), 0, 5);
			
			if (display_tri) {
				Triangle tri = new Triangle(
						new Vertex(50, 10, 0),
						new Vertex(500, 10, 0),
						new Vertex(window.input().mouseX(), window.input().mouseY(), 0)
					);
				canvas.color(RED);
				linewidth = 10;
				draw_triangle(tri, canvas);
				Triangle[] subdivided = tri.subdivide();
				for (Triangle tri_mini : subdivided) {
					canvas.color(BLUE);
					linewidth = 5;
					draw_triangle(tri_mini, canvas);
				}
			} else {
				Quad quad = new Quad(
						new Vertex(50, 				50, 				0),
						new Vertex(window.width-50, 50, 				0),
						new Vertex(window.width-50, window.height-50, 	0),
						new Vertex(50, 				window.height-50, 	0)
						);
				canvas.color(RED);
				linewidth = 10;
				draw_quad(quad, canvas);
				Triangle[] subdivided = quad.subdivide();
				for (Triangle tri_mini : subdivided) {
					canvas.color(BLUE);
					linewidth = 5;
					draw_triangle(tri_mini, canvas);
				}
			}

			canvas.draw_frame();
			window.tick();
		}
		
		window.end();
		GLState.endGLFW();
	}
	
	static int linewidth = 3;
	
	public static void draw_triangle(Triangle tri, F3DCanvas canvas) {
		Shapes.line(canvas, (int) tri.a.x, (int) tri.a.y, (int) tri.b.x, (int) tri.b.y, 0, linewidth);
		Shapes.line(canvas, (int) tri.b.x, (int) tri.b.y, (int) tri.c.x, (int) tri.c.y, 0, linewidth);
		Shapes.line(canvas, (int) tri.c.x, (int) tri.c.y, (int) tri.a.x, (int) tri.a.y, 0, linewidth);
	}
	
	public static void draw_quad(Quad quad, F3DCanvas canvas) {
		Shapes.line(canvas, (int) quad.a.x, (int) quad.a.y, (int) quad.b.x, (int) quad.b.y, 0, linewidth);
		Shapes.line(canvas, (int) quad.b.x, (int) quad.b.y, (int) quad.c.x, (int) quad.c.y, 0, linewidth);
		Shapes.line(canvas, (int) quad.c.x, (int) quad.c.y, (int) quad.d.x, (int) quad.d.y, 0, linewidth);
		Shapes.line(canvas, (int) quad.d.x, (int) quad.d.y, (int) quad.a.x, (int) quad.a.y, 0, linewidth);
	}
	
	// --- //
	
	public static float lerp(float x, float y, float t) {
		return (1 - t) * x + t * y;
	}
	
	public static Vertex lerp(Vertex a, Vertex b, float t) {
		return new Vertex(lerp(a.x, b.x, t), lerp(a.y, b.y, t), lerp(a.z, b.z, t));
	}
	
	public record Vertex(float x, float y, float z) {}
	
	public record Triangle(Vertex a, Vertex b, Vertex c) {
		public Triangle[] subdivide() {
			Triangle[] result = new Triangle[4];
			Vertex ab = lerp(a, b, 0.5f);
			Vertex ac = lerp(a, c, 0.5f);
			Vertex bc = lerp(b, c, 0.5f);
			result[0] = new Triangle(a, ab, ac);
			result[1] = new Triangle(ab, b, bc);
			result[2] = new Triangle(ac, bc, c);
			result[3] = new Triangle(ab, ac, bc);
			return result;
		}
	}
	
	public record Quad(Vertex a, Vertex b, Vertex c, Vertex d) {
		public Triangle[] subdivide() {
			Triangle[] result = new Triangle[4];
			Vertex abcd = lerp(a, c, .5f);
			result[0] = new Triangle(a, b, abcd);
			result[1] = new Triangle(a, d, abcd);
			result[2] = new Triangle(d, c, abcd);
			result[3] = new Triangle(b, c, abcd);
			return result;
		}
	}

}
