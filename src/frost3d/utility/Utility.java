package frost3d.utility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector4f;

import frost3d.enums.Alignment;
import frost3d.enums.FillMode;
import frost3d.implementations.SimpleMesh;
import frost3d.interfaces.GLMesh;

public class Utility {
	
	public static Vector4f fromHex(String hex) {
		return new Vector4f(  
				Integer.valueOf( hex.substring( 1, 3 ), 16 ) / 255f,
				Integer.valueOf( hex.substring( 3, 5 ), 16 ) / 255f,
				Integer.valueOf( hex.substring( 5, 7 ), 16 ) / 255f,
				1);
	}
	
	// css_rgba(82, 52, 145, 0.34)
	public static Vector4f fromRGBADecimal(String rgba) {
		rgba = rgba.substring(9, rgba.length()-1);
		String[] channels = rgba.split(",");
		return new Vector4f(
				Float.parseFloat(channels[0])/255,
				Float.parseFloat(channels[1])/255,
				Float.parseFloat(channels[2])/255,
				Float.parseFloat(channels[3])		// why
				);
	}

	public static double lerp(double x, double y, double t) {
		return (1 - t) * x + t * y;
	}
	
	public static String getStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	// -- Alignment/Rectangle stuff -- //
	

	/**
	 * Returns the normalized coordinates of a rectangle that will fit<br>
	 * inside of another rectangle based on the fill mode and alignment.<br><br>
	 *  
	 * For example, putting in the size of a window as the outer size, <br>
	 * the size of a framebuffer as the inner size, and setting the alignment<br>
	 * to 'middle' and the fill mode to 'contain' will return normalized coordinates<br>
	 * of a quad with the aspect ratio of the framebuffer letterboxed within a<br>
	 * rectangle with the aspect ratio of the window, aligned to the center when possible.
	 * 
	 * <br>
	 * 
	 * @param outer_width
	 * @param outer_height
	 * @param inner_width
	 * @param inner_height
	 * @param horizontal_align
	 * @param vertical_align
	 * @param fill_mode
	 * @return
	 */
	public static FloatRectangle getFittedRectangle(int outer_width, int outer_height, int inner_width, int inner_height, Alignment horizontal_align, Alignment vertical_align, FillMode fill_mode) {
		
		if (fill_mode == FillMode.CONTAIN) {
			
			float temp_width = 0;
			float temp_height = 0;
			
			// y * (x / y) = x,    this multiply(y) = x
			float window_width_ratio = outer_width / (float) outer_height; 
			float frame_width_ratio = inner_width / (float) inner_height; 		
			// If this is less than 0, y is greater than x, 
			// and the amount less than x is how much greater it is.
			// Same for vice versa.
			
			// Perfect ratio
			if (frame_width_ratio == window_width_ratio) {
				temp_width = 1;
				temp_height = 1;
			}
	
			if  (frame_width_ratio > window_width_ratio)  {
			   // Same as the next case, but match width to window width
			   // and use a height proportion instead of a width one
				temp_width = 1;
				temp_height = window_width_ratio / frame_width_ratio;
	
			} else if (frame_width_ratio < window_width_ratio) {
				// If the framebuffer is taller than it is wide,
				// match its display height to the window's height,
				// and make its display width proportional to it's own width.
				// The width is also multiplied to be proportional to the window width,
				// because the coordinates don't have any concept of aspect ratio of the window.
				temp_height = 1;
				temp_width = frame_width_ratio * (1 / window_width_ratio);
			} else if ( inner_height == inner_width ) { // perfect square
	
				// Use the original code based entirely on window size
				if ( outer_width > outer_height) {
					temp_height = 1;
					temp_width = outer_height/((float)outer_width);
				} else if ( outer_height > outer_width ) {
					temp_width = 1;
					temp_height = outer_width/((float)outer_height);
				} else if ( outer_height == outer_width ) { // perfect square
					temp_width = 1;
					temp_height = 1;
				}
				
			}
			
			// Calculate horizontal alignment
				// Default to middle. Passing in 'top' or 'bottom' can be safely ignored this way.
				float x_offset = ((1.0f-temp_width)/2.0f); 
				
				if (horizontal_align == Alignment.LEFT  ) x_offset = 0;
				if (horizontal_align == Alignment.RIGHT ) x_offset = ((1.0f-temp_width));
			
			// Calculate vertical alignment
				// Default to middle. Passing in 'left' or 'right' can be safely ignored this way.
				float y_offset = ((1.0f-temp_height)/2.0f);
				
				if (vertical_align == Alignment.TOP     ) y_offset = ((1.0f-temp_height));
				if (vertical_align == Alignment.BOTTOM  ) y_offset = 0;
			
			// Calculate actual coordinates
			float left = (x_offset);
			float right = (x_offset+temp_width);
			
			float bottom = (y_offset);
			float top = (y_offset+temp_height);
			
			// Convert from (0 to 1) to (-1 to 1)
			left = (left * 2) - 1;
			right = (right * 2) - 1;
			bottom = (bottom * 2) - 1;
			top = (top * 2) - 1;
			
			return new FloatRectangle(left, top, right, bottom);
			
		}
		
		throw new Error(Utility.class.getName() + "Fill Mode not implemented yet: " + fill_mode.toString());
	}
	
	/**
	 * [left/right/top/bottom] Are coordinates of a rectangle in a space from -1 to 1.<br>
	 * [original_normalized_coordinates] Are coordinates in that same space, but from 0 to 1.<br>
	 * What's returned are coordinates from 0 to 1 in a space inside the rectangle.<br>
	 * <br>
	 * Example:<br>
	 * The left coordinate of the rectangle is 0, AKA 0.5 from 0 to 1.<br>
	 * The right coordinate of the rectangle is 1, AKA 1 from 0 to 1.<br>
	 * The X coordinate of the input is 0.75, AKA 0.5 from -1 to 1.<br>
	 * <br>
	 * The X coordinate of the output will be 0.5, 
	 * 		because right-left=0.5,
	 * 			    original_x-left = 0.25,
	 *              and 0.25/0.5 = 0.5
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param original_normalized_coordinates
	 * @return 
	 */
	public static Vector2d getNormalizedInternalCoordinates(float left, float top, float right, float bottom, Vector2d original_normalized_coordinates) {
		
		// Coordinates are provided scaled from 0 to 1.
		double mx = original_normalized_coordinates.x();
		double my = original_normalized_coordinates.y();
	
		// X axis
		left  = (left  + 1) / 2; 	// scale from range -1 to 1 to range 0 to 1
		right = (right + 1) / 2;	// scale from range -1 to 1 to range 0 to 1
		
		mx 	  = mx    - left; 		// Subtract it so that when mx is at (left), it's at 0
		right = right - left; 		// Subtract so that 'right' is consistent with that ^
		
		mx 	 /= right; 				// Divide so that when mx is at (right), it's at 1. (right/right = 1)
		
		// mx is now in a range from 0 to 1 in relation to 'left' and 'right'.
		
		// Same thing, but for the Y axis
		top    = (top    + 1) / 2;
		bottom = (bottom + 1) / 2;
		
		my 	  -= bottom; 
		top   -= bottom;
		
		my 	  /= top;

		return new Vector2d(mx, my);
	}
	
	// TODO maybe this shouldnt be shared. also, strings.. ueghh
	static HashMap<String, SimpleMesh> mesh_queue = new HashMap<String, SimpleMesh>();

	
	/**
	 * Returns a mesh that is a quad with the specified coordinates.
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	public static SimpleMesh mesh(float left, float top, float right, float bottom, float z) {

		// Remind myself to look into this if this ever somehow comes up and becomes a memory leak or something.
		if (mesh_queue.size() > 255) {
			Log.send(Utility.class.getName() + ": Mesh cache is weirdly big. " + mesh_queue.size() + " cached meshes. Clearing queue.");
			for (String key : mesh_queue.keySet()) {
				mesh_queue.get(key).free();
			}
			mesh_queue.clear();
		}
		
		// Check queue for meshes matching these coordinates
		String name = left+":"+top+":"+right+":"+bottom+":"+z;
		SimpleMesh existing = mesh_queue.get(name);
			if (existing != null) return existing;

		// Make mesh
		SimpleMesh mesh = new SimpleMesh(new float[] {
			     right,  top, z,  // top right
			     right, bottom, z,  // bottom right
			     left, bottom, z,  // bottom left
			     left,  top, z   // top left 
				},
				new float[] {
			     1f,  1f,  // top right
			     1f,  0f,  // bottom right
			     0f,  0f,  // bottom left
			     0f,  1f,  // top left 
				},
				new int[] {
				    0, 1, 3,   // first triangle
				    1, 2, 3    // second triangle
				});
		
		mesh_queue.put(name, mesh);
		
		return mesh;
	}

	public static GLMesh mesh(FloatRectangle rect, int depth) {
		return Utility.mesh(rect.left(), rect.top(), rect.right(), rect.bottom(), depth);
	}

	public static Vector2d getNormalizedInternalCoordinates(FloatRectangle rect, Vector2d vector2d) {
		return getNormalizedInternalCoordinates(rect.left(), rect.top(), rect.right(), rect.bottom(), vector2d);
	}

	public static int clamp(int val, int min, int max) {
		if (val < min) val = min;
		if (val > max) val = max;
		return val;
	}

	
}
