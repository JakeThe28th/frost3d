package frost3d.implementations;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import frost3d.enums.IconType;
import frost3d.interfaces.F3DCanvas;
import frost3d.interfaces.F3DIconRenderer;
import frost3d.utility.Log;

public class BitmapIconRenderer implements F3DIconRenderer {
	
	HashMap<String, SimpleTexture> bitmap_icons = new HashMap<String, SimpleTexture>();
	
	private float 	scale 	= 0.75f;
	private int 	size	= 16;
	
	public void size(int s) { this.size = s; }
	public int size() { return size; }
	
	public void centered_scale(float s) { this.scale = s; }
	public float centered_scale() { return scale; }
	
	public BitmapIconRenderer() {
		File bitmap_icons_dir = new File("assets/icons/");
		for (File iconimg : bitmap_icons_dir.listFiles()) {
			try {
				bitmap_icons.put(iconimg.getName().substring(0, iconimg.getName().lastIndexOf('.')), new SimpleTexture(iconimg.getPath()));
			} catch (IOException e) {
				Log.trace(e);
			}
		}
	}
	
	public void icon(F3DCanvas canvas, int x, int y, int z, IconType icon) {
		icon(canvas, x, y, z, icon.bitmap, size, scale);
	}
	
	public void icon(F3DCanvas canvas, int x, int y, int z, IconType icon, int size) {
		icon(canvas, x, y, z, icon.bitmap, size, scale);
	}
	
	public void icon(F3DCanvas canvas, int x, int y, int z, IconType icon, int size, float scale) {
		icon(canvas, x, y, z, icon.bitmap, size, scale);
	}
	
	void icon(F3DCanvas canvas, int x, int y, int z, String name, int size, float scale) {
		if (bitmap_icons.get(name) == null) throw new Error("No such icon: " + name);
		
		x+= size/2;
		y+= size/2;
		
		size*= scale;

		x-= size/2;
		y-= size/2;
		
		canvas.rect(x, y, x+size, y+size, z, bitmap_icons.get(name));
	}

}
