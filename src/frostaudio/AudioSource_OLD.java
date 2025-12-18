package frostaudio;

import java.io.IOException;
import org.lwjgl.openal.*;

import frost3d.utility.Log;
import frostaudio.io.pcm.AudioFile;

import static org.lwjgl.openal.AL10.*;


public class AudioSource_OLD {
	
	public static final int BUFFER_SAMPLE_AMOUNT = 4410*4;
	public static final int BUFFER_THRESHOLD = 2;
	
	public static final int STATE_STOPPED = 0;
	public static final int STATE_PAUSED = 1;
	public static final int STATE_PLAYING = 2;
	
	public static int source_count = 0;
	
	// Non-static methods
	
	int source;						// OpenAL Source object for this object
	
	short[] data = new short[0]; 	// Samples array
	int[] buffers = new int[8]; 	// Available buffers
	
	int current_sample = 0; 	 	// The index of the sample after the last buffered sample
	int played_samples = 0;			// The index of the end of the samples that have been buffered and played
				
	
	int channels = 2;				// 1 for mono, 2 for stereo
	public int sample_rate = 44100;	// Samples per second
	
	int sample_amount = BUFFER_SAMPLE_AMOUNT*channels; // How much data to buffer// played_samples + alGetSourcei(source, AL11.AL_SAMPLE_OFFSET) = the current sample
	
	int state = 0; 					// Keeps track of playing / paused / stopped.
	boolean active = true;			// Used in end method.
	boolean loop = false;			// Should the source loop when it runs out of data?
	boolean auto_stop = true; 		// Whether to stop when you run out of data.
	
	int bytes_per_sample = 2;		// 16 bit PCM
	
	public AudioSource_OLD() {
		if (AudioSource_OLD.source_count < 127) {	
			AudioSource_OLD.source_count++;
			active = true;
			
	        this.source = alGenSources();
	        for (int i = 0; i < buffers.length; i++) {
	        	buffers[i] = alGenBuffers();
	        }
	        
		} else { Log.send("ERROR: Too many AudioSource's's 's"); }
	}
	
	
	/* --       << { [ ] } >> --      */
	/* --         Buffering        -- */
	/* --       << { [ ] } >> --      */
	
	
	
	/** Call this to: 
	 * <br> Restart audio if it got stopped
	 * <br> Buffer more audio */
	public void update() {
		if (state != STATE_PLAYING) return;
		
		// Start queuing from the beginning again when looping
		if (loop && current_sample >= data.length) { current_sample = 0; }
		
		//Log.send(current_sample);
		
		// Restart if stopped due to lag, stop if stopped due to source ending.
		if (ALstopped()) { 
			if (!loop && current_sample >= data.length && auto_stop) { stop(); }
			alSourcePlay(source); 
			}
		
		int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);
		
		if (processed > 0 ) {	
			
			int processed_buffer = alSourceUnqueueBuffers(source);

			played_samples += alGetBufferi(processed_buffer, AL_SIZE)/(bytes_per_sample);
			
			if (played_samples >= data.length) {
				played_samples = 0; 
				// Can't reset played_samples to the start until we actually finish the buffer,
				// whereas current_sample needs to be set to the start before we finish the buffer
				// thus, the assignments are separated to make looping work properly
			}
			
			bufferAudio(processed_buffer);
		}

	}
	
	public void bufferAudio(int buffer) {
		short[] sample_data = new short[sample_amount];
		
		if (current_sample + (sample_amount) > data.length) sample_data = new short[(data.length-current_sample)];
		for (int i = 0; i < sample_data.length; i++) {
			sample_data[i] = data[current_sample+i];
		}
	         
		current_sample += sample_data.length;
		
        //copy to buffer
        alBufferData(buffer, channels == 1 ? AL11.AL_FORMAT_MONO16 : AL11.AL_FORMAT_STEREO16, sample_data, sample_rate);

        //set up source input
        alSourceQueueBuffers(source, buffer);
        
	}
	
	
	/* --       << { [ ] } >> --      */
	/* --         Properties       -- */
	/* --       << { [ ] } >> --      */
	
	
	public void position(float x, float y, float z) {Log.send("Tried to use unbuilt method setposition hi jak "); }
	
	public void maxVolume(float limit) 		{ alSourcef(source, AL_MAX_GAIN, limit); 	}
	public void volume(float volume)   		{ alSourcef(source, AL_GAIN, volume); 		}
	public void looping(boolean value) 		{ loop = true; 								}
	public void sampleRate(int samplerate) 	{ this.sample_rate = samplerate; 			}
	public void setAutoStop(boolean b) 		{ this.auto_stop = b; 						}
	
	/** Stereo sounds weird if false. */
	public void directChannels(boolean b) 	{ alSourcei(source, SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, b ? AL_TRUE : AL_FALSE);  }
	
	
	/* --       << { [ ] } >> --      */
	/* --          Getters         -- */
	/* --       << { [ ] } >> --      */
	
	
	public boolean stopped() { return state == STATE_STOPPED; }
	public boolean paused()  { return state == STATE_PAUSED;  }
	public boolean playing() { return state == STATE_PLAYING; }
	
	public boolean ALstopped() { return alGetSourcei(source, AL_SOURCE_STATE) == AL_STOPPED; }

	/** NOTE: Stereo audio is packed one sample of left, one sample of right. */
	public short sample(int offset) { if (offset >= data.length) return 0; return data[offset]; }
	
	// It seems AL11.AL_SAMPLE_OFFSET is in *frames*, not samples. So, multiply it by two.
	public int  currentSample() 	{ return played_samples + (alGetSourcei(source, AL11.AL_SAMPLE_OFFSET)*channels); }
	public long currentTimeMillis() { return samplestoMs( currentSample() ); 							   }
		
	public int  msToSamples (long milliseconds) { return (int) 	( milliseconds * (sample_rate / 1000) ) * channels; }
	public long samplestoMs (int  sample	  ) { return 		( sample 	   / (sample_rate / 1000) ) / channels; }

	public int sampleCount() { return data.length; }
	
	public long lengthMillis() { return samplestoMs(sampleCount()); }

		
	/* --       << { [ ] } >> --      */
	/* --     PCM Data Handling    -- */
	/* --       << { [ ] } >> --      */
	
	
	/** Add audio to the end of the samples array */
	public void addAudio(short[] audio) {
		short[] newdata = new short[data.length+audio.length];
		
		for (int i = 0; i < data.length; i++)  { newdata[i] = data[i]; 				} // Copy in the old audio data
		for (int i = 0; i < audio.length; i++) { newdata[data.length+i] = audio[i]; } // Append the new audio data
		
		data = newdata;
	}
	
	/** Add audio to the end of the samples array from an audio file; <br>
	 * stereo, 44100hz, 16 bit signed 
	 * @throws IOException */
	public void addAudio(AudioFile file) throws IOException {
			this.channels = file.channels();
			if (channels >= 2) directChannels(true);
			addAudio(file.getAs16BitPCM());
			sampleRate(file.sampleRate());
	}
	
	/** Removes audio from the samples array, from the start to the end, inclusive.
	 * The byte after the byte before (start) is the location of end after this operation.<br><br>
	 * EX: start = 5, end = 10 <br>
	 * The byte at location '5' in the new array is what was at the location '11' in the old array.<br><br>
	 * 
	 * The 'current_sample' variable is updated as needed. */
	public void removeAudio(int start, int end) { Log.send("Tried to use unbuilt method removeaudio hi jak ");}
	
	
	
	/* --       << { [ ] } >> --      */
	/* -- Audio Source State Handling */
	/* --       << { [ ] } >> --      */
	
	
	/** Play audio from this source. */
	public void play() { 
		
		// Queue initial audio if stopped
		if (state == STATE_STOPPED) {
			/* Use all of the buffers the source has
			 * When alSourcePlay is called, we can't change them out anyways until the source stops.  */
			for (int buffer : buffers) { bufferAudio(buffer); }
		}
		
		alSourcePlay(source); 
		state = STATE_PLAYING;
		}
	
	/** Pause audio from this source. */
	public void pause() {
		alSourceStop(source);
		state = STATE_PAUSED;
		}
	
	/** Stop audio from this source, remove buffers, and rewind.*/
	public void stop() { 
		rewind();
		reset();
		state = STATE_STOPPED;
		} 
	
	/** Stop audio from this source, remove buffers, but don't rewind.*/
	private void reset() { 
		alSourceStop(source);
		alSourcei(source, AL_BUFFER, 0);
		state = STATE_STOPPED;
		} 
	
	/** Rewind to the beginning of this source's audio data. */
	public void rewind() { seek(0); }
	
	/** Seek to a specific time in milliseconds */
	public void seek(long milliseconds) {
		reset();
		current_sample = msToSamples(milliseconds);
		played_samples = current_sample;
		play();
	}
	
	/** Seek to a specific time in milliseconds and stop
	 * (added as a band aid fix since i'm gonna rewrite this later anyways...) */
	public void seekstop(long milliseconds) {
		reset();
		current_sample = msToSamples(milliseconds);
		played_samples = current_sample;
	}
	
	/** Make sure to call this when you're done with the source */
	public void end() {
		if (active) {
			active = false;
			AudioSource_OLD.source_count--;
			
		    //delete buffers and sources
	        alDeleteSources(source);
	        
	        for (int buffer : buffers) { alDeleteBuffers(buffer); }
			
		} else Log.send("Can't end an ended AudioSource.");
	}




	
}
