package frostaudio.averificare;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import frost3d.GLState;
import frost3d.data.BuiltinShaders;
import frost3d.implementations.SimpleCanvas;
import frost3d.implementations.SimpleTextRenderer;
import frost3d.implementations.SimpleWindow;
import frost3d.utility.Log;
import frost3d.utility.Utility;
import frostaudio.ALDevice;
import frostaudio.ALSource;
import frostaudio.AudioMixer;
import frostaudio.AudioSource_OLD;
import frostaudio.io.pcm.WAVFile;

public class DEMO_Audio {

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		
		ALDevice default_device = ALDevice.preffered();
		
		ALDevice.printInfo();
	
		//DEMO_Primary(default_device);
		//DEMO_Mixer();
		DEMO_Mixer_Visual();

//		TODO:
//			generate sine wave
//			cant regenerate buffers once queued, so...
//			queue less audio at once and have only 2 buffers
		

		
		// Test loading an audio file and playing it in the default audio device
		// Test volume, seek, etc
		// Test switching audio devices
		
		// TODO
		// Test Real time Sound Generation (sine wave...)
		// Test Real time Sound Mixing
		// Test Real Time Effects (reverb?)
		// Test Timing / make sure everything is syncd properly
		// BPM thing??
		
		default_device.end();
		
		Log.send("Demo Ended");
		
	}

	private static boolean between(long time, long start, long end) {
		return (time >= start && time <= end);
	}
	
	private static final Vector4f BLACK = new Vector4f(0,0,0,1);
	private static final Vector4f RED = new Vector4f(1,0,0,1);
	private static final Vector4f BLUE = new Vector4f(0,0,1,1);
	private static final Vector4f PURPLE = new Vector4f(1,0,1,1);
	private static final Vector4f ORANGE = new Vector4f(1,.5f,0,1);

	@SuppressWarnings("unchecked")
	private static void DEMO_Mixer_Visual() throws IOException, UnsupportedAudioFileException {
		
		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(256*5, 512, "DEMO_Mixer_Visual");
		BuiltinShaders.init();
		
		SimpleCanvas canvas = new SimpleCanvas();
			canvas.textrenderer(new SimpleTextRenderer());
		
		AudioMixer mixer = new AudioMixer();
		ALSource s = mixer.output();
		
		WAVFile song 	= new WAVFile("rgp_rain_short_nodrum.wav");
		WAVFile kick 	= new WAVFile("k.wav");
		WAVFile snare 	= new WAVFile("s.wav");
		
		long song_length = (int) s.samplestoMs(song.getAs16BitPCM().length);
		
		//mixer.insert(song.getAs16BitPCM(), 1);

		int offset = 500;

		ArrayList<AEvent> events = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			//events.add(new AEvent(offset + (song_length * i), song));
		}
		
		int beat_time = (int) ((1f / (103f / 60f)) * 1000);
		for (int i = 0; i < 60; i++) {
//			if ( i % 4 != 3) events.add(new AEvent(offset + (i * beat_time), kick));
//			if ( i % 4 == 3) events.add(new AEvent(offset + (i * beat_time), snare));
		}
		
		float scale = 1;
		float target_scale = .2f;
		
		mixer.play();

		while (!window.should_close()) {
			
			mixer.update();

			for (AEvent event : (ArrayList<AEvent>) events.clone()) {
				if (event.time < mixer.currentTimeMillis()) {
					events.remove(event);
					mixer.insert(event.event.getAs16BitPCM(), 1);
				}
			}
			
			// -- Visuals -- //
			canvas.size(window.width, window.height);
			scale = (float) Utility.lerp(scale, target_scale, 0.25f);
			
			canvas.color(BLACK);
			
			int off = 50;
			
			int sample_offset = (int) ((mixer.output().currentTimeMillis()) * scale) - window.width/2;
			
			off = -sample_offset;

			for (int time = 0; time < mixer.lengthMillis(); time += 500 / 5) {
				int xx = (int) (off + time * scale);
				canvas.rect(xx - 1, 50, xx + 1, 60, 0);
			}
			for (int time = 0; time < mixer.lengthMillis(); time += 500) {
				int xx = (int) (off + time * scale);
				canvas.rect(xx - 2, 50, xx + 2, 60, 0);
				canvas.text(xx, 20, 0, time/1000 + "." + (time % 1000) / 10 + "s");
			}
			
			int cursor = 0;
			// true cursor
			canvas.color(RED);
			cursor = (int) (off + mixer.currentTimeMillis() * scale);
			canvas.rect(cursor - 1, 70, cursor + 1, 270, 0);
			// cursor position of buffered samples
			canvas.color(ORANGE);
			int cursor_start = (int) (off + mixer.output().lastBufferTimeMillis() * scale);
			int cursor_end = (int) (off + mixer.output().nextBufferTimeMillis() * scale);
			canvas.rect(cursor_start - 1, 70, cursor_end + 1, 270, 0);
			// cursor position of played samples
			canvas.color(PURPLE);
			cursor = (int) (off + mixer.output().currentTimeMillis() * scale);
			canvas.rect(cursor - 1, 70, cursor + 1, 270, 0);
			
			int yy = (70 + 170 ) / 2;
			float height_scale = 0.01f;
			canvas.color(BLUE);
			int indiv_skip = (int) (2 / scale);
			if (indiv_skip <= 0) indiv_skip = 1;
			for (int time = 0; time < mixer.lengthMillis(); time += indiv_skip) {
				int xx = (int) (off + time * scale);
				if (xx < 0) continue;
				short height = (short) ((mixer.output().sample(mixer.output().msToSamples(time))) * height_scale);
				canvas.rect(xx - 1, yy - (height/2), xx + 1, yy + (height/2), 0);
			}
			
			if (window.input().keyPressed(GLFW.GLFW_KEY_MINUS)) target_scale /=2;
			if (window.input().keyPressed(GLFW.GLFW_KEY_EQUAL)) target_scale *=2;

			canvas.draw_frame();
			window.tick();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void DEMO_Mixer() throws IOException, UnsupportedAudioFileException {
		AudioMixer mixer = new AudioMixer();
		ALSource s = mixer.output();
		
		WAVFile song 	= new WAVFile("rgp_rain_short_nodrum.wav");
		WAVFile kick 	= new WAVFile("k.wav");
		WAVFile snare 	= new WAVFile("s.wav");
		
		long song_length = (int) s.samplestoMs(song.getAs16BitPCM().length);
		
		long time = System.currentTimeMillis() + 1000 * 20;
				
		//mixer.insert(song.getAs16BitPCM(), 1);

		mixer.play();
		
		ArrayList<AEvent> events = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			events.add(new AEvent(song_length * i, song));
		}
		
		int beat_time = (int) ((1f / (103f / 60f)) * 1000);
		for (int i = 0; i < 60; i++) {
			if ( i % 4 != 3) events.add(new AEvent(i * beat_time, kick));
			if ( i % 4 == 3) events.add(new AEvent(i * beat_time, snare));
		}

		while (System.currentTimeMillis() < time) {
			mixer.update();

			for (AEvent event : (ArrayList<AEvent>) events.clone()) {
				if (event.time < mixer.currentTimeMillis()) {
					events.remove(event);
					mixer.insert(event.event.getAs16BitPCM(), 1);
				}
			}
			
		}
	}
	
	record AEvent(long time, WAVFile event) {}
	
	private static void DEMO_Primary(ALDevice default_device) throws IOException, UnsupportedAudioFileException {
		ALSource s = new ALSource();
		s.addAudio(new WAVFile("rgp_rain_shortloop.wav"));
		s.play();
		s.loop(true);
		
		while (!s.ALstopped()) {
			if (!default_device.isConnected()) {
				Log.send("Audio Device Disconnected");
				default_device.migrateToPreferred();
			}
			s.update();
		}
	}

}
