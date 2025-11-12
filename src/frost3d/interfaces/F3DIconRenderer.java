package frost3d.interfaces;

import frost3d.enums.IconType;

public interface F3DIconRenderer {
	
	public void size(int size);
	public int size();
	
	public void icon(F3DCanvas canvas, int x, int y, int z, IconType icon);

}
