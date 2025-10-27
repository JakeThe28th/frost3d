package frost3d.interfaces;

public interface GLMesh {
	public int 	vao();
	public int 	index_count();
	public void bind();
	public void free();
}
