package frost3d.utility;

public record FloatRectangle(float left, float top, float right, float bottom) {
	
	public boolean contains(int x, int y) {
		return (x > left && x < right) && (y > top && y < bottom);
	}

	public float height() 				{ return bottom - top; }
	public float width() 					{ return right - left; }
	
	public FloatRectangle thin_all(int i) { 
		return new FloatRectangle(left + i, top + i, right - i, bottom - i); 
	}
	
	public FloatRectangle thin_horizontally(int i) {
		return new FloatRectangle(left + i, top, right - i, bottom);
	}
	
	public FloatRectangle thin_vertically(int i) {
		return new FloatRectangle(left, top + i, right, bottom - i);
	}
	
	public FloatRectangle thin_horizontally(double percent) {
		int i = (int) (width() * percent);
		return new FloatRectangle(left + i, top, right - i, bottom);
	}
	
	public FloatRectangle thin_vertically(double percent) {
		int i = (int) (height() * percent);
		return new FloatRectangle(left, top + i, right, bottom - i);
	}

	public FloatRectangle internal(float lp, float tp, float rp, float bp) {
		return new FloatRectangle(
				(int) (left + (width() * lp)), 
				(int) (top + (height() * tp)), 
				(int) (left + (width() * rp)), 
				(int) (top + (height() * bp)));
	} 
	
}
