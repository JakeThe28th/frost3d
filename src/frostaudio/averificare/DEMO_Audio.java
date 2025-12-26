package frostaudio.averificare;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import frost3d.utility.Log;
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
		DEMO_Mixer();


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
	
	@SuppressWarnings("unchecked")
	private static void DEMO_Mixer() throws IOException, UnsupportedAudioFileException {
		AudioMixer mixer = new AudioMixer();
		ALSource s = mixer.output();
		
		WAVFile song 	= new WAVFile("rgp_rain_short_nodrum.wav");
		WAVFile kick 	= new WAVFile("k.wav");
		WAVFile snare 	= new WAVFile("s.wav");
		
		long song_length = (int) s.samplestoMs(song.getAs16BitPCM().length);
		
		long time = System.currentTimeMillis() + 1000 * 20;
		
		long previous_mixtime = 0;
		
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
