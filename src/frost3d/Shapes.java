package frost3d;

import org.joml.Matrix4f;
import org.joml.Vector3d;

import frost3d.implementations.SimpleMesh;
import frost3d.implementations.SimpleTexture;
import frost3d.interfaces.F3DCanvas;
import frost3d.interfaces.GLTexture;
import frost3d.utility.Utility;

public class Shapes {
	
	public static SimpleTexture white = newWhiteTexture();

	private static SimpleTexture newWhiteTexture() {
		//BufferedImage white = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		//white.setRGB(0, 0, Integer.MAX_VALUE);
		//return new Texture(white);
		
		// using the above code with "(int) Long.MAX_VALUE)" would also work but
		// i already split the bufferedimage reading from the texture making
		// so i want to retroactively justify wasting 3 minutes
		return new SimpleTexture(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, 1, 1);
	}
	
	static SimpleMesh unit_square = new SimpleMesh(
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
	
	static SimpleMesh unit_dot = meshCircle(0, 0, 0, 0, 360, 20, 1f);
			
	public static SimpleMesh meshCircle(float x, float y, float z, double start_angle, double end_angle, int resolution, float length) {
		
		float[] vertices = new float[resolution * 3];
		int[] indices 	 = new int[(resolution*3)];
				
		// Set the first entry (entry 0) in the vertex array to be the center point of the circle
		vertices[0] = x;
		vertices[1] = y;
		vertices[2] = 0;
		
		// Get the x and y coordinates around the circle, and add them as vertices
		double angle = start_angle;
		for (int i = 1; i < resolution; i++) {
			// Set the current outer vertex
			vertices[(i*3)+0] = (float) (x + (Math.sin(Math.toRadians(angle)) * length));
			vertices[(i*3)+1] = (float) (y + (Math.cos(Math.toRadians(angle)) * length));
			vertices[(i*3)+2] = 0;
			// Indices for the current triangle
			indices[((i-1)*3)+0] = 0;	// The first set vertex is the center point
			indices[((i-1)*3)+1] = i-1; // The second set vertex is the previously made vertex
			indices[((i-1)*3)+2] = i;   // The third set vertex is the current vertex

			angle = Utility.lerp(start_angle, end_angle, ((float) i )/(resolution-2));
				// -1 because  it can only reach 14/5
				// -1 because  we include the end angle as a triangle so need an extra point
				// ^^^^^^^^^^^^^^^^^ i forgot what that means xD ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		}
		
		float[] texcoords = new float[resolution * 2]; // Defaulted to 0 I think so it's fine???
		return new SimpleMesh(vertices, texcoords, indices);
		
	}
	
	// -- Drawing stuff -- //
	
	public static void rect(F3DCanvas canvas, int left, int top, int right, int bottom, int depth) {
		Matrix4f transform = new Matrix4f().translate(left, top, depth).scale(right-left, bottom-top, 1);
		canvas.queue(unit_square, transform, white);
	}
	
	public static void rect(F3DCanvas canvas, int left, int top, int right, int bottom, int depth, GLTexture texture) {
		Matrix4f transform = new Matrix4f().translate(left, top, depth).scale(right-left, bottom-top, 1);
		canvas.queue(unit_square, transform, texture);
	}

	public static void dot(F3DCanvas canvas, int x, int y, int z, int radius) {
		Matrix4f transform = new Matrix4f().translate(x, y, z).scale(radius);
		canvas.queue(unit_dot, transform, white);
	}

	public static void line(F3DCanvas canvas, int x1, int y1, int x2, int y2, int depth, int width) {
		float distance = Utility.distance(new Vector3d(x1, y1, 0), new Vector3d(x2, y2, 0));
		Matrix4f transform = new Matrix4f()
				.translate(x1, y1, depth)
				.rotateXYZ(0, 0, (float) Utility.angle2d(x2, y1, x1, y2))
				.translate(-width/2, 0, 0) // center
				.scale(width, distance, 1);
		canvas.queue(unit_square, transform, white);
	}
	
	public static Matrix4f line_matrix(int x1, int y1, int x2, int y2, int depth, int width) {
		float distance = Utility.distance(new Vector3d(x1, y1, 0), new Vector3d(x2, y2, 0));
		Matrix4f transform = new Matrix4f()
				.translate(x1, y1, depth)
				.rotateXYZ(0, 0, (float) Utility.angle2d(x2, y1, x1, y2))
				.translate(-width/2, 0, 0) // center
				.scale(width, distance, 1);
		return transform;
	}

}