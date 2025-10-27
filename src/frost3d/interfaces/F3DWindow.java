package frost3d.interfaces;

public interface F3DWindow {
	
	public long 	identifier();
	public int 		width();
	public int 		height();
	
	public void 	tick();
	public void 	end();

	public boolean 	should_close();
	public int 		framebuffer();
	public void 	bind();

}
