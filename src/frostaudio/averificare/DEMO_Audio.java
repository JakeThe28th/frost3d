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
import frostaudio.AudioDevice;
import frostaudio.AudioSource;
import frostaudio.AudioMixer;
import frostaudio.io.pcm.WAVFile;

public class DEMO_Audio {

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		
		/*  NOTE: December 27th, 2025 
		 *  Unfortunately, my attempts at allowing real-time mixing
		 *  haven't worked out. For some reason, if 'buffer_amt' is 
		 *  too low, every couple of frames the playback skips for-
		 *  ward by that amount.
		 *  
		 *  This isn't a problem if 'buffer_amt' is sufficiently big
		 *  for some reason, so regular audio streaming is unaffected.
		 *  
		 *  But, anything that requires low latency is out of the
		 *  picture for now... */
		
		/*  In 'DEMO_Primary_Visual', some information is printed
		 *  on screen. The important piece of information here is
		 *  that the difference shown should be 0 if no bugs are
		 *  present. */
		
		AudioDevice default_device = AudioDevice.preffered();
		
		AudioDevice.printInfo();

		GLState.initializeGLFW();
		SimpleWindow window = new SimpleWindow(256*5, 312, "DEMO_Mixer_Visual");
		BuiltinShaders.init();
		
		SimpleCanvas canvas = new SimpleCanvas();
			canvas.textrenderer(new SimpleTextRenderer());
			
		// DEMO_Mixer_Visual(window, canvas);
		DEMO_Primary_Visual(default_device, window, canvas);
		
		default_device.end();
		Log.send("Demo Ended");	
	}
	
	private static final Vector4f BLACK = new Vector4f(0,0,0,1);
	private static final Vector4f RED = new Vector4f(1,0,0,1);
	private static final Vector4f BLUE = new Vector4f(0,0,1,1);
	private static final Vector4f PURPLE = new Vector4f(1,0,1,1);
	private static final Vector4f YELLOW = new Vector4f(1,1,0,1);
	record AEvent(long time, WAVFile event) {}

	@SuppressWarnings({ "unchecked", "unused" })
	@Deprecated
	private static void DEMO_Mixer_Visual(SimpleWindow window, SimpleCanvas canvas) throws IOException, UnsupportedAudioFileException {
		AudioMixer mixer = new AudioMixer();
		AudioSource s = mixer.output();
		//mixer.insert(song.getAs16BitPCM(), 1);
		WAVFile song 	= new WAVFile("rgp_rain_short_nodrum.wav");
		WAVFile kick 	= new WAVFile("k.wav");
		WAVFile snare 	= new WAVFile("s.wav");
		int  offset 		= 	    500;
		long song_length 	= (int) s.samplestoMs(song.getAs16BitPCM().length);
		int  beat_time 		= (int) ((1f / (103f / 60f)) * 1000);
		ArrayList<AEvent> events = new ArrayList<>();
		for (int i = 0; i < 4; i++) { events.add(new AEvent(offset + (song_length * i), song)); }
		for (int i = 0; i < 60; i++) {
			if ( i % 4 != 3) events.add(new AEvent(offset + (i * beat_time), kick));
			if ( i % 4 == 3) events.add(new AEvent(offset + (i * beat_time), snare));
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
			scale = (float) Utility.lerp(scale, target_scale, 0.25f);
			if (window.input().keyPressed(GLFW.GLFW_KEY_MINUS)) target_scale /=2;
			if (window.input().keyPressed(GLFW.GLFW_KEY_EQUAL)) target_scale *=2;
			canvas.size(window.width, window.height);
			draw_audio_source(canvas, mixer.output(), scale, window);
			canvas.draw_frame();
			window.tick();
		}
	}

	private static void DEMO_Primary_Visual(AudioDevice default_device, SimpleWindow window, SimpleCanvas canvas) throws IOException, UnsupportedAudioFileException {
		AudioSource s = new AudioSource();
		// s.addAudio(new WAVFile("rgp_rain_shortloop.wav"));
		s.addAudio(new WAVFile("rgp_rain_short_nodrum.wav"));
		s.loop(true);
		s.volume(0.2f);

		AudioSource kick 	= new AudioSource(); kick .addAudio(new WAVFile("k.wav"));
		AudioSource snare 	= new AudioSource(); snare.addAudio(new WAVFile("s.wav"));
		
		int beat_time = (int) ((1f / (103f / 60f)) * 1000);
		
		float scale 			= 1.0f;
		float target_scale 		= 0.2f;
		long  last_audio_time 	= 0;
		
		// vvv changing these numbers causes the bug i mentioned up there vvv ///
		s.buffferCount(2);
		s.bufferFrameAmtMS(60);
		
		s.play();

		while (!window.should_close()) {		
			long audio_time = s.currentTimeMillis();

			if (!default_device.isConnected()) {
				Log.send("Audio Device Disconnected");
				default_device.migrateToPreferred();
			}
			
			s.update();
						
			int last_target_beat = (int) ((audio_time / beat_time) * beat_time);
			if (between(last_target_beat, last_audio_time, audio_time)) {
				if ((audio_time / beat_time) % 4 != 3) { kick .seek(0); kick .play(); }
				if ((audio_time / beat_time) % 4 == 3) { snare.seek(0); snare.play(); }
			}

			scale = (float) Utility.lerp(scale, target_scale, 0.25f);
			if (window.input().keyPressed(GLFW.GLFW_KEY_MINUS)) target_scale /=2;
			if (window.input().keyPressed(GLFW.GLFW_KEY_EQUAL)) target_scale *=2;
			
			canvas.size(window.width, window.height);
			draw_audio_source(canvas, s, scale, window);
			canvas.draw_frame();
			window.tick();
			
			last_audio_time = audio_time;
		}
	}
	
	private static boolean between(long time, long start, long end) {
		return (time >= start && time < end);
	}

	private static void draw_audio_source(SimpleCanvas canvas, AudioSource s, float scale, SimpleWindow window) {
		canvas.color(BLACK);
		canvas.textrenderer().font_size(18);

		int center_y = window.height/2;
		
		int off = window.width/2;
		int sample_offset = (int) ((s.currentTimeMillis()) * scale);
		off = -sample_offset + off;

		// Unlabeled time markers
		for (int time = 0; time < s.lengthMillis(); time += 500 / 5) 
			draw_line  (off, 20, 10, canvas, time, scale);
		
		// Labeled time markers
		for (int time = 0; time < s.lengthMillis(); time += 500) 	 
			draw_cursor(off, 20, 20, canvas, time, scale, time/1000 + "." + (time % 1000) / 10 + "s");
		
		// Buffered area
		int buffer_start = x(off, s.lastBufferTimeMillis(), scale);
		int buffer_end 	 = x(off, s.nextBufferTimeMillis(), scale);
		canvas.color(YELLOW);
		if (buffer_end >= buffer_start) {
			canvas.rect(buffer_start, center_y - 30, buffer_end, center_y + 30, 0);
		} else {
			canvas.rect(buffer_start, center_y - 30, x(off, s.lengthMillis(), scale), center_y + 30, 0);
			canvas.rect(x(off, 0, scale), center_y - 30, buffer_end, center_y + 30, 0);
		}
		
		// Buffer INFO
		int true_size = (int) (s.nextBufferTimeMillis() - s.lastBufferTimeMillis());
		if (buffer_end < buffer_start) {
			true_size = (int) ((s.lengthMillis() - s.lastBufferTimeMillis()) + s.nextBufferTimeMillis());
		}
		// change 'buffer_end' for centering purposes
		if (buffer_end < buffer_start) buffer_end = x(off, s.lengthMillis(), scale);
		canvas.color(BLACK);
		canvas.textrenderer().font_size(14);
		String text = "Predicted Size: "
				  + s.buffferCount() + " buffers * " 
			      + s.bufferFrameAmtMS() + " ms per buffer = " 
				  + (s.buffferCount()*s.bufferFrameAmtMS()) + " ms";
		centerstring(canvas, buffer_start, buffer_end, center_y - 100, text);
		String text2 = "True Size: "
				  + s.nextBufferTimeMillis() + " end - " 
			      + s.lastBufferTimeMillis() + " start = " 
				  + true_size + " ms";
		centerstring(canvas, buffer_start, buffer_end, center_y - 80, text2);
		String text3 = "Difference: "
				  + (true_size - (s.buffferCount()*s.bufferFrameAmtMS()))
				  + " ms";
		centerstring(canvas, buffer_start, buffer_end, center_y - 60, text3);
		canvas.textrenderer().font_size(18);
		
		// Waveform
		float	 height_scale = 0.01f;
		int 	 indiv_skip   = (2 / scale) >= 1 ? (int) (2 / scale) : 1;
		canvas.color(BLUE);
		for (int time = 0; time < s.lengthMillis(); time += indiv_skip) {
			draw_line(off, center_y, (int) (s.sample(s.msToSamples(time)) * height_scale), canvas, time, scale);
		}
		
		// Cursors
		canvas.color(PURPLE);
		draw_cursor(off, center_y, 160, canvas, s.currentTimeMillis(), scale, "Current Time");
		canvas.color(RED);
		draw_cursor(off, center_y, 220, canvas, s.nextBufferTimeMillis(), scale, "Next");
		draw_cursor(off, center_y, 220, canvas, s.lastBufferTimeMillis(), scale, "Last");
		
	}

	static void draw_cursor(int x, int y, int h, SimpleCanvas c, long sample, float scale, String label) {
		draw_line(x, y, h, c, sample, scale);
		int cursor = x(x, sample, scale);
		c.text(cursor - c.textrenderer().size(label).x/2, y + (h/2), 0, label);
	}
	
	static void draw_line(int x, int y, int h, SimpleCanvas c, long sample, float scale) {
		int cursor = x(x, sample, scale);
		if (cursor < 0) return;
		c.rect(cursor - 1, y-(h/2), cursor + 1, y+(h/2), 0);
	}
	
	static int x(int x, long time, float scale) {
		return x + (int) (time * scale);
	}
	
	static void centerstring(SimpleCanvas canvas, int start_px, int end_px, int y, String string) {
		canvas.text(((end_px+start_px)/2)-(canvas.textrenderer().size(string).x/2), y, 0, string);
	}
	
}
