package frost3d.implementations;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2i;

import frost3d.interfaces.F3DCanvas;
import frost3d.interfaces.F3DTextRenderer;
import frost3d.interfaces.GLMesh;
import frost3d.interfaces.GLTexture;

public class SimpleTextRenderer implements F3DTextRenderer {

	/* It would probably be more efficient
	 * to only have one mesh and refer to
	 * each character with an index, and
	 * just change the texture coordinates 
	 * with uniforms instead of baking them
	 * in the mesh, but that'd overcomplicate 
	 * the shader, so the RAM cost will just 
	 * have to be eaten. */
	
	public boolean anti_aliasing_enabled = false;
	public void anti_aliasing_enabled(boolean b) {
		anti_aliasing_enabled = b;
	}
	
	int font_size = 18;
	//Font font = new Font("SansSerif", Font.PLAIN, font_size);
	Font font = new Font("Consolas", Font.PLAIN, font_size);
	
	public void font_size(int new_size) {
		this.font_size = new_size;
		font = new Font("Consolas", Font.PLAIN, font_size);
	}
	
	HashMap<Integer, TextureInfo> 	textures = new HashMap<Integer, TextureInfo>();
	
	/* Offset for drawing so it's drawn at the 'top left corner' of the character, 
	 * relative to the font size. Characters will probably have different heights 
	 * and stuff, the important part is that the location is consistent, so it 
	 * won't match the corner of some or most characters */
	public static final float CORNER_X_OFFSET = -(12f / 24f);
	public static final float CORNER_Y_OFFSET = -(16f / 24f);
	
	/* The height of a typical character in this font, relative to the font size.
	 * Again, consistency is important. Some characters like g or Q have parts
	 * that go below the usual baseline of a character, so this value is gonna
	 * be taller than most characters need. */
	public static final float CHARACTER_HEIGHT = 1.1f;
	
	/* The width of each character in this font, relative to the font size.
	 * Consolas is monospace, so the width is the same for every character. 
	 * Kerning can be dealt with later...  */
	public static final float CHARACTER_WIDTH = 0.60f;
	
	/* Same as the above, but for unsupported characters that don't use the
	 * defined font. They need a different size since otherwise they're way
	 * too small to read... All of the ones I've encountered so far other than
	 * punctuation are kanji or kana, which are pretty square, so this should
	 * be fine until proper kerning and stuff is added later. */
	public static final float UNSUPPORTED_CHARACTER_WIDTH = 1f;

	public Vector2i size(String text) {
		int xx = 0;
		for (int i = 0; i < text.length(); i++) {
			if (font.canDisplay(text.charAt(i)))  xx += font_size * CHARACTER_WIDTH;
			if (!font.canDisplay(text.charAt(i))) xx += font_size * UNSUPPORTED_CHARACTER_WIDTH;
		}
		return new Vector2i(xx, (int) (font_size * CHARACTER_HEIGHT));
	}	
	
	/** Draw one line of text, where 0, 0 is the 'top left' of the character*<br>
	 *  *Not exactly the top left, but close enough... */
	public void text(F3DCanvas canvas, int x, int y, int z, String text) {
		int xx = x + (int) (CORNER_X_OFFSET * font_size);
			y  = y + (int) (CORNER_Y_OFFSET * font_size);
		for (int i = 0; i < text.length(); i++) {
			character(canvas, xx, y, z, text.charAt(i));
			// even though it doesn't throw an error, not casting here before adding to x causes weird drift
			if (font.canDisplay(text.charAt(i)))  xx += (int) (font_size * CHARACTER_WIDTH);
			if (!font.canDisplay(text.charAt(i))) xx += (int) (font_size * UNSUPPORTED_CHARACTER_WIDTH);
		}
	}
	
	/** Draw one character of text */
	protected void character(F3DCanvas canvas, int x, int y, int z, char character) {
		canvas.queue(
				mesh(character), 
				new Matrix4f().translate(x, y, z), 
				texture()
				);
	}
	
	private GLMesh mesh(char character) {
		if (textures.get(font_size) == null) {
			textures.put(font_size, new TextureInfo(font_size));
		}
		TextureInfo info = textures.get(font_size);
		if (info.meshes.get(character) == null) {
			info.make(character);
		}
		return info.meshes.get(character);
	}

	private GLTexture texture() {
		if (textures.get(font_size) == null) {
			textures.put(font_size, new TextureInfo(font_size));
		}
		TextureInfo info = textures.get(font_size);
		return info.gltexture();
	}
		
	/** Stores texture and mesh information for a
	 *  texture sheet of characters for a specific
	 *  font size */
	class TextureInfo {
		
		public static final int CHARCOUNT = 16; // character amount = 16 * 16 = 256
		
		boolean 		changed 	= true;
		BufferedImage 	texture 	= null;
		GLTexture		gltexture 	= null;
		int 			font_size;
		
		public GLTexture gltexture() {
			if (changed) {
				if (gltexture != null) gltexture.free();
				gltexture = new SimpleTexture(texture);
				changed = false;
			}
			return gltexture;
		}
		
		HashMap<Character, GLMesh> meshes = new HashMap<Character, GLMesh>(); 
		int character_index = 0;
		
		public TextureInfo(int font_size) {
			texture = new BufferedImage(CHARCOUNT*(font_size*2), CHARCOUNT*(font_size*2), BufferedImage.TYPE_INT_ARGB);
			this.font_size = font_size;
		}

		public void make(char character) {
			changed = true;
			int char_x = character_index % CHARCOUNT;
			int char_y = character_index / CHARCOUNT;
			
			// Each character's area is (font_size*2) * (font_size*2)
			// The characters are drawn at 25% from the bottom and 25% from the left
			int offset = font_size/2;
			int unit_size = font_size*2;
			
			int unit_x = (unit_size*char_x);
			int unit_y = (unit_size*char_y);
			
			int real_x = unit_x + offset;
			int real_y = unit_y + (unit_size-offset);
			
			Font f = font.deriveFont(font_size);
			
			Graphics2D g = texture.createGraphics();
				if (anti_aliasing_enabled) {
					g.setRenderingHint(
				        RenderingHints.KEY_TEXT_ANTIALIASING,
				        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				}
				g.setColor(Color.white);
				g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (font_size))); // barely readable but we'll deal with this in the future
				if (f.canDisplay(character)) g.setFont(f);
				
		    	g.drawString(character+"", real_x, real_y);
		    	
		    int w = texture.getWidth();
		    int h = texture.getHeight();
		    
	    	SimpleMesh mesh = new SimpleMesh(new float[] {
	    			unit_size,  unit_size, 	0.0f,  // top right
	    			unit_size,  0, 			0.0f,  // bottom right
				    0, 			0, 			0.0f,  // bottom left
				    0,  		unit_size, 	0.0f   // top left 
					},
					new float[] {
					((float) (unit_x + unit_size)) / w, (((float) (unit_y + unit_size)) / h),
					((float) (unit_x + unit_size)) / w, (((float) (unit_y            )) / h),
					((float) (unit_x            )) / w, (((float) (unit_y            )) / h),
					((float) (unit_x            )) / w, (((float) (unit_y + unit_size)) / h),
					},
					new int[] {
					    0, 1, 3,   // first triangles
					    1, 2, 3    // second triangle
					});
	    	
	    	meshes.put(character, mesh);
	    	
	    	character_index++;

		}
		
	}
	
}
