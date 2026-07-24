package frost3d.averificare;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.nio.ByteBuffer;

import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleWindow;
import frost3d.interfaces.GLTexture;

public class DEMO_readwritetexthingy {

	public static void main(String[] args) {
		
		// ............... //
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(100, 100, "");
		BuiltinShaders.init();
		SimpleCanvas canvas = new SimpleCanvas();
		canvas.size(window.width, window.height);
		// ............... //
				
		
		int texture = glGenTextures();
		
		// (for my abstraction since i never added a way to draw directly with texture handles)
		GLTexture texture_wrapper = new GLTexture() {
			@Override public int gltexture() { return texture; }
			@Override public void free() { /* */ }
		};
		
		
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		

		final int tex_width = 1;
		final int tex_height = 1;
		
		ByteBuffer data = BufferUtils.createByteBuffer(4);
				   data.put((byte) 0);	
				   data.put((byte) 0);	
				   data.put((byte) 0);	
				   data.put((byte) 255);	
				   data.flip();
		
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tex_width, tex_height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
		glGenerateMipmap(GL_TEXTURE_2D);
		
		canvas.color(new Vector4f(1,1,1,1));
		
		while (!window.should_close()) {
			
			// read
			ByteBuffer outpixels =  BufferUtils.createByteBuffer(4);
			glBindTexture(GL_TEXTURE_2D, texture);
			GL11.glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, outpixels);
			
			// write
			ByteBuffer _data = BufferUtils.createByteBuffer(4);
					   _data.put((byte) (outpixels.get() + 1));	
					   _data.put((byte) (outpixels.get() + 2));	
					   _data.put((byte) 0);	
					   _data.put((byte) 255);	
					   _data.flip();
	
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tex_width, tex_height, 0, GL_RGBA, GL_UNSIGNED_BYTE, _data);
			glGenerateMipmap(GL_TEXTURE_2D);
			
			// draw the texture on a rectangle
			canvas.rect(10, 10, 90, 90, 0, texture_wrapper);
			
			canvas.draw_frame();
			window.tick();
		}
		
	}

}
