package frost3d.enums;

public enum CursorType {
	ARROW_CURSOR,
	IBEAM_CURSOR,
	CROSSHAIR_CURSOR,
	POINTING_HAND_CURSOR,
	/** The horizontal resize/move arrow shape. 						*/ 	RESIZE_EW_CURSOR, 
	/** The vertical resize/move arrow shape.							*/ 	RESIZE_NS_CURSOR, 
	/** The top-left to bottom-right diagonal resize/move arrow shape. 	*/ 	RESIZE_NWSE_CURSOR, 
	/** The top-right to bottom-left diagonal resize/move arrow shape. 	*/ 	RESIZE_NESW_CURSOR, 
	/** The omni-directional resize/move cursor shape. 					*/ 	RESIZE_ALL_CURSOR, 
	/** The operation-not-allowed shape. 								*/ 	NOT_ALLOWED_CURSOR 
}