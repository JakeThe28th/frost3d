package frostaudio.io.pcm;

import javax.sound.sampled.UnsupportedAudioFileException;

public interface AudioFile {
			
	/** Gets the audio data and converts it to 16 bit PCM data if needed. <br>
	 * May be stereo or mono, check with channels()  
	 * @throws UnsupportedAudioFileException **/
	public short[] getAs16BitPCM();
	
	/** Gets the number of channels in this audio file. */
	public int channels();
	
	//public NBTCompound toNBT();
	
	//public void fromNBT(NBTCompound compound);

	public int sampleRate();

}
