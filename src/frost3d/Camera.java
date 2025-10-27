package frost3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import frost3d.interfaces.F3DWindow;

/**
 * Created to solve the issue of handling multiple world/projection matrices in the Graphics class.<br>
 * You can create a world, which is perspective or orthographic (based on the size of a framebuffer/or window),<br>
 * in addition to having camera functionality like global transforms.
 */
public class Camera {
	
	public static final float ORTHO_FAR = 100000f;
	
	float x = 0, y = 0, z = 0;
	float rx = 0, ry = 0, rz = 0;
	
	Matrix4f world_matrix = null;
	public Matrix4f movement_matrix = new Matrix4f();
		
	public Camera() { ortho(); }
	public Camera(int ww, int hh) { ortho(ww, hh); }
	public Camera(Framebuffer frame) { ortho(frame); }
	public Camera(F3DWindow frame) { ortho(frame); }

	boolean is_ortho = false;
	int width = 0;		// Width in units, not pixels
	int height = 0;		// Height in units, not pixels
	
	int horizontal_offset = 0; // Usually 0, used with normalized coordinates.
	int vertical_offset = 0; // Usually 0, used with normalized coordinates.
	
	float fov = 0;

	public void setWorldMatrix(Matrix4f new_matrix) { this.world_matrix = new_matrix; }
	
	/**
	 * Set the world matrix to a normalized orthographic matrix
	 * @param frame
	 */
	public void ortho() {
		is_ortho = true;
		width = 2;
		height = -2;
		
		horizontal_offset = -1;
		vertical_offset = -1;
		
		world_matrix = new Matrix4f().ortho(-1, 1, -1, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	/**
	 * Set the world matrix to an orthographic matrix where each unit corresponds to 1 pixel of (frame)
	 * @param frame
	 */
	public void ortho(Framebuffer frame) {
		is_ortho = true;
		width = frame.width();
		height = frame.height();
		horizontal_offset = 0;
		vertical_offset = 0;
		world_matrix = new Matrix4f().ortho(0,frame.width(),frame.height(),0, -100000, ORTHO_FAR);
	}
	
	/**
	 * Set the world matrix to an orthographic matrix where each unit corresponds to 1 pixel of (frame)
	 * @param frame
	 */
	public void ortho(F3DWindow frame) {
		is_ortho = true;
		width = frame.width();
		height = frame.height();
		horizontal_offset = 0;
		vertical_offset = 0;
		world_matrix = new Matrix4f().ortho(0,frame.width(),frame.height(),0, -100000, ORTHO_FAR);
	}
	
	/**
	 * Set the world matrix to an orthographic matrix where each unit corresponds to 1 pixel of a frame with resolution (width, height)
	 * @param frame
	 */
	public void ortho(int width, int height) {
		this.width = width;
		this.height = height;
		horizontal_offset = 0;
		vertical_offset = 0;
		is_ortho = true;
		world_matrix = new Matrix4f().ortho(0,width,height,0, -100000, ORTHO_FAR);
	}
	
	/**
	 * Set the world matrix to a perspective matrix.
	 * zNear and zFar are set to .001 and 1000 respectively.
	 * @param frame
	 */
	public void perspective(F3DWindow frame, float fov) {
		is_ortho = false;
		width = frame.width();
		height = frame.height();
		horizontal_offset = 0;
		vertical_offset = 0;
		this.fov = fov;
		world_matrix = 	new Matrix4f().perspective(
				(float)Math.toRadians(fov),
				((float) frame.width())/frame.height(), .001f, 1000f);

	}
	
	/**
	 * Set the world matrix to a perspective matrix.
	 * zNear and zFar are set to .001 and 1000 respectively.
	 * @param frame
	 */
	public void perspective(Framebuffer frame, float fov) {
		is_ortho = false;
		width = frame.width();
		height = frame.height();
		horizontal_offset = 0;
		vertical_offset = 0;
		this.fov = fov;
		world_matrix = 	new Matrix4f().perspective(
				(float)Math.toRadians(fov),
				((float) frame.width())/frame.height(), .001f, 1000f);

	}
	
	public Matrix4f getMatrix() { 
		movement_matrix.identity();
		movement_matrix.rotateXYZ(rx, ry, rz);
		movement_matrix.translate(x, y, z);
		
		
		return new Matrix4f(this.world_matrix).mul(movement_matrix); }
	
	/** Set the position of the 'camera' in the world */
	public void position(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/** Set the rotation of the 'camera' in the world */
	public void rotation(float x, float y, float z) {
		this.rx = x;
		this.ry = y;
		this.rz = z;
	}
	
	public Vector3f rotation() {
		return new Vector3f(rx, ry, rz);
	}
	
	public Vector3f position() {
		return new Vector3f(x, y, z);
	}
	
	public float width() { return width; }
	public float height() { return height; }
	
	public float horizontal_offset() { return horizontal_offset; }
	public float vertical_offset() { return vertical_offset; }

}
