package frost3d.averificare;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;

import frost3d.Framebuffer;
import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleTexture;
import frost3d.implementations.SimpleWindow;
import frost3d.interfaces.F3DCanvas;
import frost3d.interfaces.GLTexture;
import frost3d.utility.Log;
import frost3d.utility.Utility;

public class DEMO_RayGen {
	
	static float FRAME_COUNT = 16;
	
	public static void main(String[] args) {

		GLState.initializeGLFW();
		
		SimpleWindow window = new SimpleWindow(512, 512, "RayGen");
		BuiltinShaders.init();
		SimpleCanvas wincanvas = new SimpleCanvas();

		SimpleCanvas canvas = new SimpleCanvas();
		SimpleCanvas canvas_16 = new SimpleCanvas();
		SimpleCanvas canvas_16_2 = new SimpleCanvas();
		SimpleCanvas canvas_2 = new SimpleCanvas();
		SimpleCanvas canvas_export = new SimpleCanvas();

		Vector4f c = Utility.fromHex("#C832FF");
		c.w = 0;
		
		canvas.clear_color(c);	
		canvas.adopt(new Framebuffer(48,48));
		
		canvas_2.color(new Vector4f(1,1,1,1));
		canvas_2.clear_color(c);	
		canvas_2.adopt(new Framebuffer(48,48));
		
		canvas_16.color(new Vector4f(1,1,1,1));
		canvas_16.adopt(new Framebuffer(16,48));
		canvas_16.clear_color(c);	
		
		canvas_16_2.color(new Vector4f(1,1,1,1));
		canvas_16_2.adopt(new Framebuffer(16,48));
		canvas_16_2.clear_color(c);	
		
		GLTexture ray = null;
		
		try { 
			ray = new SimpleTexture("light_ray.png"); 
		} catch (IOException _) {
			Log.send("Failed to load texture.");
		}
		
		int count = -1;
		
		boolean export = true;
		
		canvas_export.color(new Vector4f(1,1,1,1));
		canvas_export.adopt(new Framebuffer(48,(int) (48*FRAME_COUNT*2)));
		canvas_export.clear_color(c);	
		canvas_export.draw_frame();
		canvas_export.draw_frame();
					
		while (!window.should_close()) {
			
			
//			canvas.color(new Vector4f(1,0,0,1));
//			canvas.rect(16, 0, 32, 48, 0);
			
			count ++;
			if (count > FRAME_COUNT * 2) {
				if (export) break;
				count = 0;
			}
			
			float t = count / FRAME_COUNT;
			
			GL21.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE);  
			
			ray(24 - 6, 0, (float) (Math.sin(t * Math.PI + .13f) + 1) / 2, ray, canvas);
			ray(24 + 6, 0, (float) (Math.sin(t * Math.PI + .93f) + 1) / 2, ray, canvas);
			ray(24, 0, (float) (Math.sin(t * Math.PI + 1.32f) + 1) / 2, ray, canvas);

			
			ray(24 - 3, 0, (float) (Math.sin(t * Math.PI + Math.PI + .13f) + 1) / 2, ray, canvas);
			ray(24 + 3, 0, (float) (Math.sin(t * Math.PI + Math.PI  + .93f) + 1) / 2, ray, canvas);

			canvas.draw_frame();
			
			// tiling stuff
			
			canvas_2.rect(0, 48, 48, 0, 0, canvas.framebuffer().texture());
			canvas_2.rect(-24+8, 48, 24+8, 0, 0, canvas.framebuffer().texture());
			canvas_2.rect(24-8, 48, 72-8, 0, 0, canvas.framebuffer().texture());
			canvas_2.draw_frame();
			
			canvas_16.rect(-16, 0, 32, 48, 0, canvas_2.framebuffer().texture());
			canvas_16.draw_frame();
			
//			canvas_16_2.rect(-8, 0, 8, 48, 0, canvas_16.framebuffer().texture());
//			canvas_16_2.rect(8, 0, 24, 48, 0, canvas_16.framebuffer().texture());
//			canvas_16_2.draw_frame();
			
			canvas.color(new Vector4f(1,1,1,1));
			canvas.rect(16, 0, 32, 48, 0, canvas_16.framebuffer().texture());
			canvas.draw_frame();

			// ...
			
			if (export) {
				canvas_export.rect(0, 48 * (count), 48, 48 * (count + 1), 0, canvas.framebuffer().texture());
				canvas_export.draw_frame(false);
			}
			
			wincanvas.size(window.width, window.height);
			wincanvas.color(new Vector4f(1,1,1,1));
			wincanvas.rect(wincanvas.size(), 0, canvas.framebuffer().texture());
			wincanvas.draw_frame();

			window.tick();
		}
		
		save_image("light_ray_multi_anim.png", canvas_export);

		// extender
		BufferedImage sheet = get_img(canvas_export);
		BufferedImage extender = new BufferedImage(sheet.getWidth(), sheet.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = extender.getGraphics();
		for (int i = 0; i < FRAME_COUNT * 2; i++) {
			g.drawImage(sheet, 0, i*48, 48, (i+1)*48, 0, i*48, 48, (i*48) + 1, null);
		}
		try {
			ImageIO.write(extender, "png", new File("light_ray_multi_anim_Extender" + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ...

		window.end();

		GLState.endGLFW();
		
	}

	private static void ray(int x, int y, float opacity, GLTexture ray, SimpleCanvas canvas) {
		//Log.send(opacity);
		canvas.color(new Vector4f(1,1,1,(opacity ) / 2));
		canvas.rect(x-24, 48-y, (x-24)+48, 0, y, ray);
	}
		
	
	// (copied from RGP)
	static private void save_image(String filename, F3DCanvas canvas) {
			try {
				ImageIO.write(get_img(canvas), "png", new File(filename + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	static private BufferedImage get_img(F3DCanvas canvas) {
			canvas.framebuffer().bind();
			int[] pixels = new int[canvas.width()*canvas.height()];
			GL11.glReadPixels(0, 0, canvas.width(), canvas.height(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
//			ByteBuffer write = ByteBuffer.wrap(new byte[pixels.length*4]);
//			write.order(ByteOrder.LITTLE_ENDIAN);
//			for (int id : pixels) write.putInt(id);
			BufferedImage img = new BufferedImage(canvas.width(), canvas.height(), BufferedImage.TYPE_INT_ARGB);
			for (int ind = 0; ind < pixels.length; ind++) {
				int pxl = 0x00000000;
				    pxl |= (pixels[ind] & 0x00FF0000) >> 16;
				    pxl |= (pixels[ind] & 0x0000FF00);
				    pxl |= (pixels[ind] & 0x000000FF) << 16;
				    pxl |= (pixels[ind] & 0xFF000000);
				img.setRGB(ind % canvas.width(), canvas.height() - ((ind / canvas.width()) + 1), pxl);
			}
			return img;
		}

}
