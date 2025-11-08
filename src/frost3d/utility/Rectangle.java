package frost3d.utility;

import org.joml.Vector2i;

public record Rectangle(int left, int top, int right, int bottom) {

	public boolean contains(int x, int y) {
		return (x > left && x < right) && (y > top && y < bottom);
	}
	
	public boolean contains(Vector2i p) {
		return contains(p.x, p.y);
	}

	public int height() 				{ return bottom - top; }
	public int width() 					{ return right - left; }
	
	public Rectangle thin_all(int i) { 
		return new Rectangle(left + i, top + i, right - i, bottom - i); 
	}
	
	public Rectangle thin_horizontally(int i) {
		return new Rectangle(left + i, top, right - i, bottom);
	}
	
	public Rectangle thin_vertically(int i) {
		return new Rectangle(left, top + i, right, bottom - i);
	}
	
	public Rectangle thin_horizontally(double percent) {
		int i = (int) (width() * percent);
		return new Rectangle(left + i, top, right - i, bottom);
	}
	
	public Rectangle thin_vertically(double percent) {
		int i = (int) (height() * percent);
		return new Rectangle(left, top + i, right, bottom - i);
	}

	public Rectangle internal(float lp, float tp, float rp, float bp) {
		return new Rectangle(
				(int) (left + (width() * lp)), 
				(int) (top + (height() * tp)), 
				(int) (left + (width() * rp)), 
				(int) (top + (height() * bp)));
	}

	public Rectangle constrain_to(Rectangle other) {
		int left 	= this.left;
		int top 	= this.top;
		int right 	= this.right;
		int bottom 	= this.bottom;
		if (left 	< other.left) 	left = other.left;
		if (top 	< other.top) 	top = other.top;
		if (right 	> other.right) 	right = other.right;
		if (bottom 	> other.bottom) bottom = other.bottom;
		return new Rectangle(left, top, right, bottom);
	}

	public boolean intersects(Rectangle other) {
		return 	(right 	> other.left) 	&&
				(left 	< other.right) 	&&
				(bottom > other.top) 	&&
				(top 	< other.bottom);
	}

	public Rectangle offset(int offset_x, int offset_y) {
		return new Rectangle(left + offset_x, top + offset_y, right + offset_x, bottom + offset_y);
	} 
	
}
