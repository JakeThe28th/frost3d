package frost3d.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Log {
	
	static String log = "";
	
	public static boolean print_log = true;
	
	private static void internal_log(String msg) {
		log += msg + "\n";
		
		if (!print_log) return;
		
		try {
			Files.writeString(Paths.get("latest.log"), log);
		} catch (IOException e) {
			System.out.println("failed to save log");
			e.printStackTrace();
		}
	}
	
	public static void send(String... strings) {
		String logged_message = "";
		String p = "";
		
		for (String s : strings) {
			logged_message += p + s;
			p = ", ";
		}
		
		System.out.println(logged_message);
		internal_log(logged_message);
	}

	public static void send(float... floats) {
		String logged_message = "";
		String p = "";
		
		for (float f : floats) {
			logged_message += p  + f;
			p = ", ";
		}
		
		System.out.println(logged_message);
		internal_log(logged_message);
	}
	
	public static void send(double... doubles) {
		String logged_message = "";
		String p = "";
		
		for (double d : doubles) {
			logged_message += p  + d;
			p = ", ";
		}
		
		System.out.println(logged_message);
		internal_log(logged_message);
	}

	public static void trace(Throwable e) {
		Log.send("[EXCEPTION] " + e.getMessage());
		Log.send(Utility.getStackTrace(e));
	}

}
