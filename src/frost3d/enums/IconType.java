package frost3d.enums;

public enum IconType {
	CONTROL_MODE_LOOP_ALL		("loop", 				"control_mode_loop_all"),
	CONTROL_MODE_LOOP_NONE		("play_once", 			"control_mode_loop_none"),
	CONTROL_MODE_LOOP_ONCE		("loop_once", 			"control_mode_loop_one"),
	CONTROL_MODE_SHUFFLE		("shuffle", 			"control_mode_shuffle"),
	CONTROL_PAUSE				("pause", 				"control_pause"),
	CONTROL_PIN					("pin", 				"control_pin"),
	CONTROL_PIN_CANCEL			("x", 					"control_pin_cancel"),
	CONTROL_PLAY				("play", 				"control_play"),
	CONTROL_SKIP_NEXT			("next", 				"control_skip_next"),
	CONTROL_SKIP_PREVIOUS		("previous", 			"control_skip_previous"),
	CONTROL_STOP				("stop", 				"control_stop"),
	CONTROL_VOLUME				("volume", 				"control_volume"),
	FAVORITE_DINOSAUR			("trash", 				"favorite_dinosaur"),
	FAVORITE_DINOSAUR_OUTLINE	("trash", 				"favorite_dinosaur_outline"),
	FAVORITE_HEART				("heart", 				"favorite_heart"),
	FAVORITE_HEART_OUTLINE		("heart", 				"favorite_heart_outline"),
	FAVORITE_STAR				("star", 				"favorite_star"),
	FAVORITE_STAR_OUTLINE		("star", 				"favorite_star_outline"),
	GENERIC_ARROW_DOWN			("down", 				"generic_arrow_down"),
	GENERIC_ARROW_LEFT			("left", 				"generic_arrow_left"),
	GENERIC_ARROW_RIGHT			("play", 				"generic_arrow_right"),
	GENERIC_ARROW_UP			("up", 					"generic_arrow_up"),
	GENERIC_ARROWS_VERTICAL		("up_down_arrow", 		"generic_arrows_vertical"),
	GENERIC_CLIPBOARD_COPY		("copy", 				"generic_clipboard_copy"),
	GENERIC_CLIPBOARD_PASTE		("paste", 				"generic_clipboard_paste"),
	GENERIC_EDIT				("pencil", 				"generic_edit"),
	GENERIC_HAMBURGER			("hamburger", 			"generic_hamburger"),
	GENERIC_HOME				("home", 				"generic_home"),
	GENERIC_PLUS				("+", 					"generic_plus"),
	GENERIC_SEARCH				("magnifying_glass", 	"generic_search"),
	GENERIC_RELOAD				("reload", 				"generic_reload"),
	GENERIC_SETTINGS			("gear", 				"generic_settings"),
	GENERIC_SQUARE_OUTLINE		("hollow_square", 		"generic_square_outline"),
	GENERIC_TRASH				("giant_trash", 		"generic_trash"),
	GENERIC_VISIBILITY_OFF		("eye", 				"generic_visibility_off"),
	GENERIC_VISIBILITY_ON		("eye", 				"generic_visibility_on");
	public String vector; public String bitmap;
	IconType(String vector, String bitmap) {
		this.vector = vector; this.bitmap = bitmap;
	}
}