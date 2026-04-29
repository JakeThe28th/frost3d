package frost3d.utility;

import org.lwjgl.glfw.GLFW;

public record SimpleWindowHint(Keys hint, boolean value) {
	
	public enum Keys {
		RESIZABLE				(GLFW.GLFW_RESIZABLE), 
		VISIBLE					(GLFW.GLFW_VISIBLE), 
		DECORATED				(GLFW.GLFW_DECORATED), 
		FOCUSED					(GLFW.GLFW_FOCUSED), 
		AUTO_ICONIFY			(GLFW.GLFW_AUTO_ICONIFY),
		FLOATING				(GLFW.GLFW_FLOATING),
		MAXIMIZED				(GLFW.GLFW_MAXIMIZED),
		CENTER_CURSOR			(GLFW.GLFW_CENTER_CURSOR),
		TRANSPARENT_FRAMEBUFFER	(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER),
		FOCUS_ON_SHOW			(GLFW.GLFW_FOCUS_ON_SHOW),
		SCALE_TO_MONITOR		(GLFW.GLFW_SCALE_TO_MONITOR);
		int hint_id;
		public int hint_id() { return hint_id; }
		Keys(int hint_id) {
			this.hint_id = hint_id;
		}
	}

}
