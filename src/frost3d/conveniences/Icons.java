package frost3d.conveniences;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;

import frost3d.Shapes;
import frost3d.implementations.*;
import frost3d.interfaces.*;
import frost3d.utility.Log;

public class Icons {


	static HashMap<String, GLMesh> icons = new HashMap<String, GLMesh>();
	
	// Load the icons at startup
	
	static {
		
		String icons_file = "";
		
		try { icons_file = Files.readString(Paths.get("assets/icons_triangulated.obj")); } 
		catch (IOException e) { throw new Error("Failed to read icons file"); }
		
		// Parse OBJ file
		String[] lines = icons_file.split("\n");
		
		// Icons have no textures or shading, 
		// so normals and texture coordinates
		// can be ignored
		
		record Vertex(float x, float y, float z) {};
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		
		record Triangle(int[] vertex_indices) {};
		ArrayList<Triangle> current_triangles = new ArrayList<Triangle>();
		
		String current_object_name = null;
		
		for (int line = 0; line <= lines.length; line++) {
			
			String[] split;
			if (line == lines.length) {
				// without this and the <= in the loop condition,
				// the loop would end without actually saving the last object
				// kinda scuffed but this forces it to 
				split = new String[] {"o", "DUMMY_OBJECT"};
			} else {
				split = lines[line].strip().split(" ");
			}
			
			if (split[0].startsWith("#")) continue; // Comment
			if (split[0].equals("o")) {
				if (current_object_name != null) {
					// Last object is done being defined, 
					// so convert it to a Mesh
					
					// 3 vertices per triangle, 3 coordinates per vertex
					ArrayList<Vertex> mesh_verts_temp = new ArrayList<Vertex>();
					int[]   mesh_indices = new int  [ (current_triangles.size() * 3)];
					ArrayList<Integer> index_mapping = new ArrayList<Integer>();
					for (int i = 0; i < current_triangles.size(); i ++) {
						Triangle triangle = current_triangles.get(i);
						
						for (int j = 0; j < 3; j++) {
							if (index_mapping.indexOf(triangle.vertex_indices[j]) == -1) {
								mesh_verts_temp.add(vertices.get(triangle.vertex_indices[j]));
								index_mapping.add(triangle.vertex_indices[j]);
							}
							mesh_indices[(i*3)+j] = index_mapping.indexOf(triangle.vertex_indices[j]);
						}
					}
					
					float[] mesh_verts = new float[mesh_verts_temp.size()*3];
					for (int i = 0; i < mesh_verts_temp.size(); i++) {
						Vertex vertex = mesh_verts_temp.get(i);
						mesh_verts[(i*3) + 0] = vertex.x;
						mesh_verts[(i*3) + 1] = vertex.y;
						mesh_verts[(i*3) + 2] = vertex.z;
					}
					
					icons.put(current_object_name, new SimpleMesh(
							mesh_verts, 
							new float[(current_triangles.size()*3)*2], 
							mesh_indices)
							);
					
					Log.send("Loaded icon '" + current_object_name + "'");
					
				}
				// Reset for next object
				current_triangles = new ArrayList<Triangle>();
				current_object_name = split[1];
			}
			
			if (split[0].equals("v")) { // Vertex
				vertices.add(new Vertex(
						Float.parseFloat(split[1]),
						Float.parseFloat(split[2]),
						Float.parseFloat(split[3])
						));
			}
			
			if (split[0].equals("f")) { // Face
				current_triangles.add(new Triangle(new int[] {
						// Only the first part of the v/vt/n is taken
						// since we're ignoring texture coordinates and normals
						// Also, subtracting one because for some reason OBJ files count from 1 and not 0...
						Integer.parseInt(split[1].split("/")[0])-1,
						Integer.parseInt(split[2].split("/")[0])-1,
						Integer.parseInt(split[3].split("/")[0])-1
						}));
			}
		}
		
	}
	
	// Drawing //
	
	public static void icon(F3DCanvas canvas, int x, int y, int z, String name, int size) {
		if (Icons.icons.get(name) == null) throw new Error("No such icon: " + name);
		
		float scale = size;
		canvas.queue(
				Icons.icons.get(name), 
				new Matrix4f()
					// Icons are made centered in Blender, 
				    // so they're offset by 0.5
					.translate(x + (scale/2f), y+(scale/2f), z) 
					 // negative size cuz i didn't realize i was modeling the icons upside down....
					.scale(size, -size, size),
				Shapes.white);
	}
	
}
