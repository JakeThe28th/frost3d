package frostaudio.io.pcm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import frost3d.utility.Log;

public class OGGFile implements AudioFile {
	
	public OGGFile(String filename) throws IOException, UnsupportedAudioFileException {
		File file = new File(filename);
		byte[] bytes = Files.readAllBytes(file.toPath());		
		readOGG(bytes);
	}

	/** Reads this file from binary data. 
	 * @throws UnsupportedAudioFileException */
	public OGGFile(byte[] bytes) throws UnsupportedAudioFileException { readOGG(bytes); }
	
	short channels = 0;
	short bits_per_sample = 0;
	int sample_rate = 0;
	
	short[] pcm;
	
	public void readOGG(byte[] data) throws UnsupportedAudioFileException {
		// ngl the [demo](...) for this is weirdly overcomplicated and kinda misleading
		// luckily the docs exist
		// [https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/stb/Vorbis.java]
		try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer 	channels 		= stack.mallocInt(1);
            IntBuffer 	sample_rate 	= stack.mallocInt(1);
    		ByteBuffer 	bytes 			= toByteBuffer(data);
    		ShortBuffer pcm_raw 		= STBVorbis.stb_vorbis_decode_memory(bytes, channels, sample_rate);
    		pcm = new short[pcm_raw.limit()];
    		for (int i = 0; i < pcm.length; i++) pcm[i] = pcm_raw.get();
            this.channels 	 = (short)  channels.get();
            this.sample_rate = 			sample_rate.get();
        }	
	}
	
	/** Why can't i just use ByteBuffer.wrap(data)? No one knows. */
	private ByteBuffer toByteBuffer(byte[] data) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	@Override
	public short[] getAs16BitPCM() {
		return pcm;
	}

	@Override
	public int channels() { return channels; }
	
	@Override
	public int sampleRate() { return sample_rate; }
	
}
