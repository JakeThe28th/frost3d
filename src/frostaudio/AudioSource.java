package frostaudio;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_BUFFERS_PROCESSED;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_MAX_GAIN;
import static org.lwjgl.openal.AL10.AL_SIZE;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_STOPPED;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetBufferi;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceQueueBuffers;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourceUnqueueBuffers;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

import java.io.IOException;

import org.lwjgl.openal.AL11;
import org.lwjgl.openal.SOFTDirectChannels;

import frost3d.utility.Log;
import frostaudio.io.pcm.AudioFile;

public class AudioSource {
	
	static final int GLOBAL_SOURCE_LIMIT = 127;
	static 		 int global_source_count = 0;
	
	// -- == global properties == -- //

	static final int BYTES_PER_SAMPLE = 2; 									 // 16-bit PCM
	
	// -- == instance properties == -- //
	
	private int 	channels 			= 2;								 // 1 for mono, 2 for stereo
	private int 	sample_rate			= 44100;							 // Samples per second

	private int 	buffer_count 		= 8;								 // The number of buffers to cycle between
	private int 	buffer_amt 			= ((int) ( 44.1 * 400) ) * channels; // The number of PCM samples to buffer at once.

	protected int 	next_sample 		= 0; 	// The index of the sample after the last buffered sample
	protected int 	last_sample 		= 0;	// The index of the end of the samples that have been buffered and played
	
	private boolean loop 				= false;							 // If the source loops when out of data.
	private boolean auto_stop			= true;								 // Automatically run stop() when out of data.

	// -- == setters == -- //
	
	public 	void 	channels		 (int count) 			 	 { channels = count; }
	public 	void 	sampleRate		 (int rate) 			 	 { sample_rate = rate; }
	public 	void 	loop			 (boolean value)    	 	 { loop = value; }
	public 	void 	auto_stop		 (boolean value)    	 	 { auto_stop = value; }
	public	void 	position		 (float x, float y, float z) { Log.send("Unimplemented method :: ALSource.position"); }
	public  void 	maxVolume		 (float limit) 				 { alSourcef(source, AL_MAX_GAIN, limit); }
	public  void 	volume			 (float volume) 			 { alSourcef(source, AL_GAIN, 	 volume); }
	
	public  void	bufferFrameAmtMS (int millis)				 { bufferFrameAmt((int) ((sample_rate/1000f) * millis)); }
	public  void	bufferFrameAmt	 (int amount)				 { buffer_amt = amount * channels; }

	public void 	buffferCount	 (int count ) { 
		if (playing) pause();
		freeBuffers();
		buffer_count = count;
		buffers 	 = new int[buffer_count];
		generateBuffers();
	}
	
	/** https://openal-soft.org/openal-extensions/SOFT_direct_channels.txt <br>
	 *  Without this enabled, stereo audio sounds weird...  */
	public  void    directChannels	 (boolean b) { 
		alSourcei(source, SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, b ? AL_TRUE : AL_FALSE); 
	}
	
	// -- == getters == -- //
	
	public int 	buffferCount	 () { return buffer_count; }
	public int 	bufferFrameAmtMS () { return (int) samplestoMs(buffer_amt); }

	public boolean ALstopped() { return alGetSourcei(source, AL_SOURCE_STATE) == AL_STOPPED; }
	
	/** NOTE: Stereo audio is packed one sample of left, one sample of right. */
	public short sample(int offset) { if (offset >= data.length) return 0; return data[offset]; }
	
	// It seems AL11.AL_SAMPLE_OFFSET is in *frames*, not samples. So, multiply it by two.
	public int  currentTimeSamples() { return last_sample + (alGetSourcei(source, AL11.AL_SAMPLE_OFFSET) * channels); }
	public long currentTimeMillis () { return samplestoMs(currentTimeSamples()); }
	
	public long lastBufferTimeMillis() { return samplestoMs(last_sample); }
	public long nextBufferTimeMillis() { return samplestoMs(next_sample); }

	public int 	sampleCount 	  () { return data.length; }
	public int 	frameCount 	  	  () { return data.length / channels; }
	public long lengthMillis	  () { return samplestoMs(sampleCount()); }
	
	public int  msToSamples (long milliseconds) { return (int) 	( milliseconds * (sample_rate / 1000) ) * channels; }
	public long samplestoMs (int  sample	  ) { return 		( sample 	   / (sample_rate / 1000) ) / channels; }
	
	// -- == data == -- //

	int 	source;										// Identifier of the OpenAL source of this object.
	
	short	[] data    = new short	[0]; 				// Samples array
	int		[] buffers = new int	[buffer_count]; 	// Available buffers
	
	public AudioSource() {
		if (AudioSource.global_source_count < GLOBAL_SOURCE_LIMIT) {
			AudioSource.global_source_count ++;
			this.source = alGenSources();
			generateBuffers();
		} else {
			Log.send("Failed to create new audio source :: Too many");
		}
	}
	
	/** Fills the buffer array. <br>
	 * NOTE: this method does *not* free the buffers first.
	 * Make sure to call freeBuffers() beforehand to avoid memory leaks... */
	private void generateBuffers() {
	 for (int i = 0; i < buffers.length; i++) {
        	buffers[i] = alGenBuffers();
        }
	}
	
	/** Deletes the buffers and source object associated with this ALSource.<br>
	 *  The effect of any method calls after this are undefined. It will probably just crash. */
	public void free() {
		AudioSource.global_source_count --;
		alDeleteSources(source);
		freeBuffers();
	}
	
	/** Deletes the buffers associated with this ALSource.<br>
	 *  You can recover from this by calling generateBuffers(). */
	private void freeBuffers() {
		if (buffers != null) for (int buffer : buffers) alDeleteBuffers(buffer); 
		    buffers  = null;
	}
	
	
	// -- == PCM Data Handling == -- //
	
	/** Add audio to the end of the samples array */
	public void addAudio(short[] audio) {
		short[] newdata = new short[data.length+audio.length];
		for (int i = 0; i < data.length; i++) { newdata[i] 				 = data[i]; } // Copy in the old audio data
		for (int i = 0; i < audio.length  ; i++) { newdata[data.length+i] = audio  [i]; } // Append the new audio data
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
	 * The 'next_sample' variable is updated as needed. */
	public void removeAudio(int start, int end) { Log.send("Unimplemented method :: ALSource.removeAudio");}
	
	
	// -- {{ ==      ................................................................................    == }} -- //
	
	                                          // -- == playback == -- //
	
	boolean playing = false;
	
	// -- == getters == -- //
	
	public boolean playing() { return playing; }
	
	/** Play audio from this source without buffering any audio initially. */
	public void ALplay() { 
		if (ALstopped()) alSourcePlay(source); 
		playing = true;
	}

	/** Play audio from this source. */
	public void play() { 
		// Buffer the initial audio for this source.
		if (ALstopped() || !playing) {
			reset(); // make sure the source is empty
			last_sample = next_sample;
			for (int buffer : buffers) { bufferAudio(buffer); }
		}
		alSourcePlay(source); 
		playing = true;
	}
	
	/** Pause audio from this source. */
	public void pause() {
		next_sample = currentTimeSamples();
		last_sample = next_sample;
		reset();
	}
	
	/** Stop audio from this source, remove buffers, and rewind.*/
	public void stop() { 
		seek(0);
	} 
	
	/** Stop audio from this source, remove buffers, but don't rewind.*/
	private void reset() { 
		alSourceStop(source);
		alSourcei(source, AL_BUFFER, 0);
		playing = false;
	} 
	
	/** Seek to a specific time in milliseconds. */
	public void seek(long milliseconds) {
		reset();
		next_sample = msToSamples(milliseconds);
		last_sample = next_sample;
	}
	
	/** Seek to a specific time in milliseconds without stopping the source. */
	public void seekplay(long milliseconds) {
		seek(milliseconds);
		play();
	}
	
	/** This method is responsible for refilling used buffers and restarting 
	 *  the audio source. It should be called as often as possible to minimize
	 *  audio cuts. If calling this method often isn't possible, try increasing 
	 *  the buffer size and/or amount instead. */
	//public void update() { while (update_internal() > 0); }
	public void update() { update_internal(); }

	/** 'next_sample' needs to check and be set to 0 for looping after every call to bufferAudio(). 
	 * I don't feel like wrapping this in a while loop and having ugly indentation, so I split the method instead... */
	private int update_internal() {
		if (!playing) return 0;
		
		int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);
		
		// Start queuing from the beginning again when looping
		if (loop && next_sample >= data.length) next_sample = 0;
	
		// Stop if no more samples are left. 
		if (!loop && next_sample >= data.length && ALstopped() && auto_stop) 	stop(); 
		// Restart if stopped due to lag
		else if (ALstopped()) 									     			play();
				
		if (processed > 0 ) {	
			int processed_buffer = alSourceUnqueueBuffers(source);
			last_sample += alGetBufferi(processed_buffer, AL_SIZE) / (BYTES_PER_SAMPLE);
			if (loop && last_sample >= data.length) last_sample = 0; 
			// NOTE: calling 'bufferAudio()' increments 'next_sample' by the buffer's size
			bufferAudio(processed_buffer);
		}
		
		return processed;
	}
	
	/** Queues audio data into the audio source by storing it in the provided buffer. */
	public void bufferAudio(int buffer) {
		if (channels == 2 && (buffer_amt & 1) != 0) {
			throw new IllegalStateException("Buffer amount isn't divisible by [channels], " + buffer_amt + "/" + channels);
		}

		// Store either 'buffer_sample_count' or the remaining amount samples, into 'sample_data', whichever is fewer.
		short[] sample_data = new short[(next_sample + buffer_amt > data.length) ? data.length-next_sample : buffer_amt];
		for (int i = 0; i < sample_data.length; i++) sample_data[i] = data[next_sample+i];
		next_sample += sample_data.length;
        // Copy 'sample_data' to the buffer, and then queue that buffer.
        alBufferData(buffer, channels == 1 ? AL11.AL_FORMAT_MONO16 : AL11.AL_FORMAT_STEREO16, sample_data, sample_rate);
        alSourceQueueBuffers(source, buffer);
	}
	
}