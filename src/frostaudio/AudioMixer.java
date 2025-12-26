package frostaudio;

import frost3d.utility.Log;

/** NOTE: Audio is assumed stereo. */
public class AudioMixer {
	
	int max_data_length;
	
	ALSource mix_output;

	public ALSource output() {
		return mix_output;
	}
	
	public AudioMixer() {
		mix_output = new ALSource();
		mix_output.buffferCount(2);
		mix_output.bufferFrameAmtMS(500);
		mix_output.auto_stop(false);
		mix_output.channels(2);
		mix_output.directChannels(true);
		max_data_length = mix_output.msToSamples(1000);
	}
	
	/** Mixes samples into the output as soon as possible:<br>
	 *  - If the source is stopped, then the first sample of the
	 *    inserted data will overlap the current sample index.<br>
	 *  - If the source is being played, then the first sample
	 *    of the inserted data will be played on the next unfilled
	 *    buffer swap. */
	public void insert(short[] sample_data, float volume) {
		int begin_index = mix_output.next_sample;
		
		int required_size = begin_index + sample_data.length;
		
		// Resize the sample data array if it's too small.
		if (required_size > mix_output.data.length) {
			short[] newdata = new short[required_size];
			for (int i = 0; i < mix_output.data.length; i++) { newdata[i] = mix_output.data[i]; }
			mix_output.data = newdata;
		}
		
		// Actually insert the data.
		for (int i = 0; i < sample_data.length; i++) {
			// AFAIK, a float can exactly represent any short value,
			// so casting back to short here should lose anything
			// (https://stackoverflow.com/questions/3793838)
			float original_sample 			 = mix_output.data[begin_index + i];
			float insert_sample 	  		 = sample_data[i] * volume;
			float new_sample 				 = original_sample + insert_sample;
			if (new_sample > Short.MAX_VALUE) new_sample = Short.MAX_VALUE; // ...clipping...
			mix_output.data[begin_index + i] = (short) new_sample;
		}
		
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
		if ((mix_output.lengthMillis() - mix_output.currentTimeMillis()) <= 100) {
			insert(noise(mix_output.msToSamples(1000)), 1);
			Log.send("Added");
		}
		if (mix_output.frameCount() > max_data_length) {
			short[] new_data = new short[max_data_length];
			for (int i = 0; i < new_data.length && mix_output.last_sample+i < mix_output.data.length ; i++) {
				new_data[i] = mix_output.data[mix_output.last_sample+i];
			}
			mix_output.next_sample -= mix_output.last_sample;
			mix_output.last_sample = 0;
		}
		mix_output.update();
		//mix_output.ALplay();
	}

	private short[] noise(int length) {
		short[] data = new short[length];
		for (int i = 0; i < data.length; i+=2) {
			short val = (short) (Math.random() * (float) (Short.MAX_VALUE / 8));
			data[i] = val;
			data[i+1] = val;
		}
		return data;
	}

	public long lengthMillis() {
		return currentTimeMillis() + (mix_output.lengthMillis() - mix_output.currentTimeMillis());
	}
	
}
