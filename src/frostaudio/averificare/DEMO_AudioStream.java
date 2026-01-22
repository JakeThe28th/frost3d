package frostaudio.averificare;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.glfw.GLFW;

import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleTextRenderer;
import frost3d.implementations.SimpleWindow;
import frost3d.utility.Log;
import frost3d.utility.Utility;
import frostaudio.AudioDevice;
import frostaudio.AudioStreamedSource;
import frostaudio.io.pcm.WAVFile;

public class DEMO_AudioStream {

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		
		AudioDevice default_device = AudioDevice.preffered();
		
		AudioDevice.printInfo();

		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(256, 312, "DEMO_Streamed");
		BuiltinShaders.init();
		
		SimpleCanvas canvas = new SimpleCanvas();
			canvas.textrenderer(new SimpleTextRenderer());
			
		DEMO_Mixer_Visual(window, canvas);
		
		default_device.end();
		Log.send("Demo Ended");	
	}
	
	record AEvent(long time, WAVFile event) {}

	@SuppressWarnings({ "unchecked" })
	private static void DEMO_Mixer_Visual(SimpleWindow window, SimpleCanvas canvas) throws IOException, UnsupportedAudioFileException {
		AudioStreamedSource stream = new AudioStreamedSource();
		//mixer.insert(song.getAs16BitPCM(), 1);
		WAVFile song 	= new WAVFile("rgp_rain_short_nodrum.wav");
		WAVFile kick 	= new WAVFile("k.wav");
		WAVFile snare 	= new WAVFile("s.wav");
		int  offset 		= 	    500;
		long song_length 	= (int) stream.samplestoMs(song.getAs16BitPCM().length);
		int  beat_time 		= (int) ((1f / (103f / 60f)) * 1000);
		ArrayList<AEvent> events = new ArrayList<>();
		for (int i = 0; i < 4; i++) { events.add(new AEvent(offset + (song_length * i), song)); }
		for (int i = 0; i < 60; i++) {
			if ( i % 4 != 3) events.add(new AEvent(offset + (i * beat_time), kick));
			if ( i % 4 == 3) events.add(new AEvent(offset + (i * beat_time), snare));
		}
		float scale = 1;
		float target_scale = .2f;
		stream.start();
		while (!window.should_close()) {
			stream.tick();
			for (AEvent event : (ArrayList<AEvent>) events.clone()) {
				if (event.time < stream.currentTimeMillis()) {
					events.remove(event);
					stream.insert(event.event.getAs16BitPCM(), 1);
				}
			}
			scale = (float) Utility.lerp(scale, target_scale, 0.25f);
			if (window.input().keyPressed(GLFW.GLFW_KEY_MINUS)) target_scale /=2;
			if (window.input().keyPressed(GLFW.GLFW_KEY_EQUAL)) target_scale *=2;
			canvas.size(window.width, window.height);
			canvas.draw_frame();
			window.tick();
		}
	}

}
