package frost3d;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import java.util.HashMap;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;

import frost3d.implementations.SimpleMesh;
import frost3d.implementations.SimpleTexture;
import frost3d.interfaces.GLTexture;
import frost3d.utility.Log;

/* NOTE: Rendering framebuffers to a window is now handled 
 * by PositionedCanvas. You can't do that here anymore... */
public class Framebuffer {
	
	int fbo = 0;
	GLTexture color_texture = null;
	GLTexture depth_texture = null;
	
	int width;
	int height;
		
	HashMap<String, SimpleMesh> mesh_queue = new HashMap<String, SimpleMesh>();
	
	public Framebuffer(int width, int height) { 
		this(width, height, true);
	}
	
	public Framebuffer(int width, int height, boolean has_alpha) { 
		this.width =  width;
		this.height = height;
		
		this.fbo = glGenFramebuffers();
		bind();
			
		// Make an empty RGBA texture to render to
		this.color_texture = new SimpleTexture(); // The texture is bound upon creation, no need to call bind().
			// Empty texture data. 
			int image_type = GL40.GL_RGBA;
			if (!has_alpha) image_type = GL40.GL_RGB;
			SimpleTexture.texImage2D(GL_TEXTURE_2D, width, height, image_type, GL_UNSIGNED_BYTE, 0);
			// Attach the texture to the Framebuffer
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, color_texture.gltexture(), 0);
			
		// Make a depth buffer
		this.depth_texture = new SimpleTexture(); // The texture is bound upon creation, no need to call bind().
			// Empty texture data. Why float?
			SimpleTexture.texImage2D(GL_TEXTURE_2D, width, height, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
			// Attach the depth buffer to the framebuffer
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth_texture.gltexture(), 0);
			
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE)
			Log.send("Framebuffer completed!");
			else Log.send("Failure.");
		
	}
	
	public GLTexture texture() { return color_texture; }
	
	public int fbo() { return fbo; }
	
	public void bind() { 
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		GL30.glViewport(0, 0, this.width(), this.height()); 
	}
	
	public void free() { 
		glDeleteFramebuffers(fbo);
		color_texture.free();
		depth_texture.free(); 
	}
	
	public int width() { return width; }
	public int height() { return height; }

}
