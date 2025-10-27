package frost3d.implementations;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import org.lwjgl.opengl.GL40;

import frost3d.interfaces.GLMesh;

/** 
 * An immutable mesh, containing only the bare minimum.
 * Essentially, a VBO holder. 
 */
public class SimpleMesh implements GLMesh {
	
	int vao = -1;

	int vbo = -1; 	// Vertices
	int tbo = -1; 	// Texture coordinates
	int ibo = -1; 	// Indices
	int cbo = -1; 	// Colors (TODO)
	
	int count = -1;

	public SimpleMesh(float[] vertices, float[] texcoords, int[] indices) {
		
		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		vbo = bufferFloats(vertices, 0, 3);
		tbo = bufferFloats(texcoords, 1, 2);
		
		ibo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);  
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		
		count = indices.length;
		
		glBindVertexArray(0);
	}
	
	private static int bufferFloats(float[] data, int location, int size) {
		int abo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, abo);  
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		
		glVertexAttribPointer(location, size, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(location);  
		
		return abo;
	}
	
	public int vao() { return vao; }
	public void bind() { glBindVertexArray(vao); }
	public int index_count() { return count; }
	public void free() { 
		GL40.glDeleteVertexArrays(vao);
		GL40.glDeleteBuffers(vbo);
		GL40.glDeleteBuffers(tbo);
		GL40.glDeleteBuffers(ibo);
		GL40.glDeleteBuffers(cbo);
	}

}
