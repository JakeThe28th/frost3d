package frostaudio;

import static org.lwjgl.openal.AL10.*;

import java.util.ArrayList;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.SOFTDirectChannels;

import frost3d.utility.Log;

/** An audio source which has no concept of a start or end.<br> 
 * <br>
 *  More specifically, this audio source can be started and stopped,
 *  and can have audio added at the current time, but cannot be
 *  rewound.<br>
 *  <br>
 *  It's intended for mixing multiple stereo audio streams into one,
 *  in realtime. It was easier to make this its own thing
 *  instead of force that onto AudioSource...
 *  */
public class AudioStreamedSource {
	
	private int channels 	= 2;								 // 1 for mono, 2 for stereo
	private int sample_rate	= 44100;							 // Samples per second
	private int buffer_amt 	= ((int) ( 44.1 * 20) ) * channels;  // The number of PCM samples to buffer at once.

	public int  msToSamples (long milliseconds) { return (int) 	( milliseconds * (sample_rate / 1000) ) * channels; }
	public long samplestoMs (int  sample	  ) { return 		( sample 	   / (sample_rate / 1000) ) / channels; }
	
	// -- OpenAL State -- //
	private int source;
	private int front_buffer;
	private int back_buffer;
	
	public AudioStreamedSource() {
		if (AudioSource.global_source_count < AudioSource.GLOBAL_SOURCE_LIMIT) {
			AudioSource.global_source_count ++;
			this.source = alGenSources();
			this.front_buffer = alGenBuffers();
			this.back_buffer = alGenBuffers();
			alSourcei(source, SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT, AL_TRUE); 
		} else {
			Log.send("Failed to create new audio source :: Too many");
		}
	}
	
	public void free() {
		AudioSource.global_source_count --;
		alDeleteSources(source);
		alDeleteBuffers(front_buffer);
		alDeleteBuffers(back_buffer);
	}
	
	ArrayList<ArrayList<Short>> packets = new ArrayList<>();
	
	boolean playing = false;
	
	public boolean ALstopped() { return alGetSourcei(source, AL_SOURCE_STATE) == AL_STOPPED; }
	public boolean ALinitial() { return alGetSourcei(source, AL_SOURCE_STATE) == AL10.AL_INITIAL; }

	long start_time = 0;
	
	public long currentTimeMillis() {
		if (!playing) return 0;
		return System.currentTimeMillis() - start_time;
	}

	public void start() {
		playing = true;
		start_time = System.currentTimeMillis();
	}
	
	public void stop() {
		alSourceStop(source);
		alSourcei(source, AL_BUFFER, 0);
		playing = false;
	}
	
	public void tick() {
		if (!playing) return;
		if (ALstopped() || ALinitial()) {
			alSourcei(source, AL_BUFFER, 0); // <-- without this, 'alSourcePlay' resets to the start of the old buffer
			buffer(front_buffer);
			buffer(back_buffer);
			if (ALstopped() || ALinitial()) alSourcePlay(source); 
		}	
		if (alGetSourcei(source, AL_BUFFERS_PROCESSED) > 0) buffer(alSourceUnqueueBuffers(source));
	}
	
	private void buffer(int buffer) {
		if (packets.size() <= 0) return;
		short[] sample_data = new short[packets.getFirst().size()];
		for (int i = 0; i < sample_data.length; i++) sample_data[i] = packets.getFirst().get(i);
        alBufferData(buffer, channels == 1 ? AL11.AL_FORMAT_MONO16 : AL11.AL_FORMAT_STEREO16, sample_data, sample_rate);
        alSourceQueueBuffers(source, buffer);
        packets.removeFirst();
	}
	
	/** Mixes samples into the output as soon as possible:<br>
	 *  - If the source is stopped, then the first sample of the
	 *    inserted data will overlap the current sample index.<br>
	 *  - If the source is being played, then the first sample
	 *    of the inserted data will be played on the next unfilled
	 *    buffer swap. */
	public void insert(short[] sample_data, float volume) {
		int packet_count = Math.ceilDiv(sample_data.length, buffer_amt);
		ArrayList<ArrayList<Short>> split_data = new ArrayList<>();
		for (int p = 0; p < packet_count; p++) {
			ArrayList<Short> packet_data = new ArrayList<Short>();
			for (int i = 0; i < buffer_amt; i++) {
				int target_index = (buffer_amt * p) + i;
				if (target_index < sample_data.length) {
					packet_data.add(sample_data[target_index]);
				} else {
					packet_data.add((short) 0);
				}
			}
			split_data.add(packet_data);
		}
		merge(split_data);
	}
	
	private void merge(ArrayList<ArrayList<Short>> new_packets) {
		for (int i = 0; i < new_packets.size(); i++) {
			if (i < packets.size()) {
				for (int s = 0; s < buffer_amt; s++) {
					packets.get(i).set(s, (short) (packets.get(i).get(s) + new_packets.get(i).get(s)));
				}
			} else {
				packets.add(new_packets.get(i));
			}
		}
	}
	
}
