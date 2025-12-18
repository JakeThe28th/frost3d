package frostaudio.averificare;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import frost3d.utility.Log;
import frostaudio.AudioDevice;
import frostaudio.AudioSource_OLD;
import frostaudio.io.pcm.WAVFile;

public class DEMO_Audio {

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		
		AudioDevice default_device = AudioDevice.preffered();
		
		AudioDevice.printInfo();
		
		AudioSource_OLD s = new AudioSource_OLD();
		s.addAudio(new WAVFile("rgp_minor_conversation2.wav"));
		
		
		
		s.play();
		
		while (!s.ALstopped()) {


			if (!default_device.isConnected()) {
				Log.send("Audio Device Disconnected");
				default_device.migrateToPreferred();
			}
			s.update();
		}
		
		// Test loading an audio file and playing it in the default audio device
		
		// Test volume, seek, etc
		
		// Test switching audio devices
		
		// TODO
		
		// Test Real time Sound Generation (sine wave...)
		
		// Test Real time Sound Mixing
		
		// Test Real Time Effects (reverb?)
		
		// Test Timing / make sure everything is syncd properly
		
		// BPM thing??

		/*
		 * 
		 * ALState.bind(DEFAULT_DEVICE_NAME)
		 * 
		 * AudioDevice device = new AudioDevice(DEFAULT_DEVICE_NAME);
		 * device.bind
		 * 
		 * 
		 */
		
		default_device.end();
		
		Log.send("Demo Ended");
		
	}

}
