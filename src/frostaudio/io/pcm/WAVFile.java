package frostaudio.io.pcm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

import javax.sound.sampled.UnsupportedAudioFileException;

import frost3d.utility.Log;


/** 
 * Wave Format is Cool Actually
It's only now that I go back to fix my WAV implementation that I realize how cool it is...

It consists of chunks, each with: a 4 character header in ASCII, and an integer length

followed by some unique data based on the chunk type.

If you don't know what a chunk is, you can skip it by moving forward it's length.


Because of this coolness, I can see
	Unknown WAVE chunk "JUNK", skipping forward 28 bytes.
	Unknown WAVE chunk "smpl", skipping forward 60 bytes.
	Unknown WAVE chunk "cue ", skipping forward 28 bytes.
	Unknown WAVE chunk "tlst", skipping forward 28 bytes.
And simply ignore them.

*/
public class WAVFile implements AudioFile {
	
	public WAVFile(String filename) throws IOException, UnsupportedAudioFileException {
		File file = new File(filename);
		byte[] bytes = Files.readAllBytes(file.toPath());
		
		readWAV(bytes);
	}

	/** Reads this file from binary data. 
	 * @throws UnsupportedAudioFileException */
	public WAVFile(byte[] bytes) throws UnsupportedAudioFileException { readWAV(bytes); }
	
	short channels = 0;
	short bits_per_sample = 0;
	int sample_rate = 0;
	
	byte[] pcm;
	
	/** Temporary variable to store 4-byte identifiers. */
	byte[] magic = new byte[4];

	/**
	 * https://www.mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
	 * @throws UnsupportedAudioFileException 
	 * 
	 *  */
	public void readWAV(byte[] data) throws UnsupportedAudioFileException {
		ByteBuffer buff = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
				
		// All WAV files start with a RIFF chunk.
		buff.get(magic); // 4 bytes (RIFF)
		
		if (!(new String(magic).equals("RIFF"))) throw new UnsupportedAudioFileException("Expected RIFF at start of .wav file");
		readRIFFChunk(buff);
		
	}
	
	public void readRIFFChunk(ByteBuffer buff) throws UnsupportedAudioFileException {
		
		// Size of master chunk
		@SuppressWarnings("unused")
		int RIFF_chunk_size = buff.getInt(); // 4 bytes
		
		// WAVE identifier
		buff.get(magic);				// 4 bytes (WAVE)
		if (!(new String(magic).equals("WAVE"))) throw new UnsupportedAudioFileException("Expected WAVE in .wav file");

		// Read the subsequent chunks:
		
		while (buff.hasRemaining()) {
			
			// Next chunk identifier (4 bytes)
			buff.get(magic);
			
			if ((new String(magic).equals("fmt "))) readfmtChunk(buff);
			else if ((new String(magic).equals("data"))) readDataChunk(buff);
			//else if ((new String(magic).equals("LIST"))) readLISTChunk(buff); <-- I don't use the data anyways, let it skip.
			//else if ((new String(magic).equals("fact"))) readFactChunk(buff); <-- I don't use the data anyways, let it skip.
			else {
				int chunk_size = buff.getInt(); 
				Log.send("Unknown WAVE chunk \"" + new String(magic) + "\", skipping forward " + chunk_size + " bytes.");
				buff.position(buff.position()+chunk_size);
			}

		}
		
	}
	
	public void readDataChunk(ByteBuffer buff) throws UnsupportedAudioFileException {
		int chunk_size = buff.getInt(); 
		
		// get data
		this.pcm = new byte[chunk_size]; 
			// 12 bytes for 'fmt ', 'WAVE', and the thing telling me its 16 bytes
			// 16 bytes for the data
		    // list_chunk_size bytes for the list chunk
		    // 8 for LIST and the thing telling me how long list chunk is
		    // 4 for 'data'
		
		buff.get(pcm);
	}
	
	/** https://stackoverflow.com/questions/63929283/what-is-a-list-chunk-in-a-riff-wav-header */
	public void readLISTChunk(ByteBuffer buff) throws UnsupportedAudioFileException {
		int chunk_size = buff.getInt(); 
		
		// skip over ascii data about lmms blah blah
		buff.position(buff.position()+chunk_size);		 
	}
	
	public void readFactChunk(ByteBuffer buff) throws UnsupportedAudioFileException {
		@SuppressWarnings("unused")
		int chunk_size = buff.getInt(); 
		@SuppressWarnings("unused")
		int dwSampleLength = buff.getInt();
	}
	
	public void readfmtChunk(ByteBuffer buff) throws UnsupportedAudioFileException {

		// Next 4 bytes
		int chunk_size = buff.getInt(); 
		
		if (chunk_size != 16) 
		if (chunk_size != 40) throw new UnsupportedAudioFileException("Non 16 or 40 chunk-size wav file is not supported");
		
		@SuppressWarnings("unused")
		int format = buff.getShort(); 			// 2 bytes
		this.channels = buff.getShort(); 		// 2 bytes
		
		this.sample_rate = buff.getInt();  	 	// 4 bytes
		@SuppressWarnings("unused")
		int data_rate = buff.getInt(); 			// 4 bytes
		
		@SuppressWarnings("unused")
		int data_block_size = buff.getShort(); 	// Next 2 bytes
		this.bits_per_sample = buff.getShort(); // Next 2 bytes
		
		if (bits_per_sample != 16 && bits_per_sample != 24 && bits_per_sample != 8) throw new UnsupportedAudioFileException("Unsupported wav bit depth: " + bits_per_sample);
		
		if (chunk_size == 40) 	buff.position(buff.position()+(40-16)); // skip over the rest its probably junk	
				
	}


	@Override
	public short[] getAs16BitPCM() {
		if (bits_per_sample == 16) {
			short[] pcmn = new short[pcm.length/2];
			ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcmn);
			return pcmn;
		}

		if (bits_per_sample == 24) {
			short[] pcmn = new short[pcm.length/3];
			//ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcmn);
			
			ByteBuffer pcm_b = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < pcmn.length; i++) {
				pcm_b.get(); // Skip the least significant byte of the 24 bit number
				pcmn[i] = pcm_b.getShort(); // Read the two most significant bytes as a short
				
			}
			
			return pcmn;
		}
		return null;
	}


	@Override
	public int channels() { return channels; }
	
	@Override
	public int sampleRate() { return sample_rate; }
/*
	@Override
	public NBTCompound toNBT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromNBT(NBTCompound compound) {
		// TODO Auto-generated method stub
		
	}
	*/
}
