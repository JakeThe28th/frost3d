package frost3d.conveniences;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import frost3d.enums.IconType;
import frost3d.implementations.SimpleTexture;
import frost3d.interfaces.F3DCanvas;
import frost3d.utility.Log;

public class BitmapIcons {

	// Load the bitmap icons at startup
	
	static HashMap<String, SimpleTexture> bitmap_icons = new HashMap<String, SimpleTexture>();
	
	static {
		File bitmap_icons_dir = new File("assets/icons/");
		for (File iconimg : bitmap_icons_dir.listFiles()) {
			try {
				bitmap_icons.put(iconimg.getName().substring(0, iconimg.getName().lastIndexOf('.')), new SimpleTexture(iconimg.getPath()));
			} catch (IOException e) {
				Log.trace(e);
			}
		}
	}
	
	public static void icon(F3DCanvas canvas, int x, int y, int z, IconType icon, int size, float scale) {
		BitmapIcons.icon(canvas, x, y, z, icon.bitmap, size, scale);
	}
	
	static void icon(F3DCanvas canvas, int x, int y, int z, String name, int size, float scale) {
		if (bitmap_icons.get(name) == null) throw new Error("No such icon: " + name);
		
		x+= size/2;
		y+= size/2;
		
		size*= scale;

		x-= size/2;
		y-= size/2;
		
		canvas.rect(x, y, x+size, y+size, z, bitmap_icons.get(name));
	}

}
