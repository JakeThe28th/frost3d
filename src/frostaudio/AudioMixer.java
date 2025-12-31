package frostaudio;

/** NOTE: Audio is assumed stereo. */
@Deprecated 
public class AudioMixer {
	
	static class AudioBuffer {
		int back_buffer_size; 	// how many samples of played audio to retain
		int front_buffer_size;	// how many samples of unplayed audio to store
		short[] data;

		int next_sample;		// the index of the next unplayed sample.
		
		public void buffersize(int back, int front) {
			back_buffer_size 	= back;
			front_buffer_size 	= front;
			data = new short[back_buffer_size + front_buffer_size];
		}
		
		public AudioBuffer(AudioSource mix) {
			buffersize(mix.msToSamples(3000), mix.msToSamples(3000));
		}
		
		public void update(AudioSource mix) {
			mix.data = data;
			
			int old_sample = mix.next_sample;
			mix.update();
			int new_sample = mix.next_sample;
			
			int old_end_of_front_buffer = next_sample + front_buffer_size;
			
			// Didn't loop, so just increment by the difference
			if (new_sample > old_sample) next_sample += (new_sample-old_sample); 
			
			// Looped, so add the difference between the old sample and the end, + the new sample and the start
			if (new_sample < old_sample) next_sample += (data.length - old_sample) + (new_sample); 
			
			// Clear the space between the old front buffer's end and new one.
			for (int i = old_end_of_front_buffer; i < next_sample + front_buffer_size; i++) {
				data[i % data.length] = 0;
			}
		}
		
		public void insert(short[] sample_data, float volume, int offset) {
			// Actually insert the data.
			for (int i = 0; i < sample_data.length && i < front_buffer_size; i++) {
				// AFAIK, a float can exactly represent any short value,
				// so casting back to short here should lose anything
				// (https://stackoverflow.com/questions/3793838)
				float original_sample 			 = data[(next_sample + i) % data.length];
				float insert_sample 	  		 = sample_data[i] * volume;
				float new_sample 				 = original_sample + insert_sample;
				if (new_sample > Short.MAX_VALUE) new_sample = Short.MAX_VALUE; // ...clipping...
				data[(next_sample + i) % data.length] = (short) new_sample;
			}
		}
	}
		
	AudioSource 	mix_output;
	AudioBuffer mix_buffer;

	public AudioSource output() { return mix_output; }
	
	public AudioMixer() {
		mix_output = new AudioSource();
		mix_output.buffferCount(2);
		mix_output.bufferFrameAmtMS(1000);
		mix_output.auto_stop(false);
		mix_output.channels(2);
		mix_output.directChannels(true);
		mix_output.loop(true);
		mix_buffer = new AudioBuffer(mix_output);
	}
	
	/** Mixes samples into the output as soon as possible:<br>
	 *  - If the source is stopped, then the first sample of the
	 *    inserted data will overlap the current sample index.<br>
	 *  - If the source is being played, then the first sample
	 *    of the inserted data will be played on the next unfilled
	 *    buffer swap. */
	public void insert(short[] sample_data, float volume) {
		mix_buffer.insert(sample_data, volume, 0);
	}

	boolean playing 	= false; 
	long 	start_time 	= 0;
	
	public void play() {
		playing = true;
		start_time = System.currentTimeMillis();
		mix_output.play();
	}
	
	public long currentTimeMillis() {
		return System.currentTimeMillis() - start_time; // mix_output.currentTimeMillis();
	}

	public void update() {
		mix_buffer.update(mix_output);
	}
	
}
