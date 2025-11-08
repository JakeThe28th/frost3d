package frostaudio.io.pcm;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

public class RAWAudioFile implements AudioFile {
	
	private short[] pcm;
	
	public RAWAudioFile() {}
	
	
	public RAWAudioFile(String filename) throws IOException {
		File file = new File(filename);
		byte[] bytes = Files.readAllBytes(file.toPath());
		
		this.pcm = new short[bytes.length/2];
		// to turn bytes to shorts 
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcm);
	}

	/** Reads this file from binary data. ( Little Endian )*/
	public RAWAudioFile(byte[] bytes) {
		this.pcm = new short[bytes.length/2];
		// to turn bytes to shorts 
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcm);
	}

	@Override
	public short[] getAs16BitPCM() { return pcm; }

	@Override
	public int channels() { return 2; }
/*
	@Override
	public NBTCompound toNBT() {
		byte[] writeData = new byte[pcm.length*2];
		
		ByteBuffer bb = ByteBuffer.allocate(pcm.length * 2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		for (short sample : pcm) { bb.putShort(sample); }
		
		bb.position(0);
		bb.get(writeData);
		
		NBTCompound compound = new NBTCompound();
			compound.put("pcm", new NBTByteArray(writeData));
			compound.put("format", "raw");
			compound.put("channels", new NBTInt(2));
		
		return compound;
	}

	@Override
	public void fromNBT(NBTCompound compound) {		
		byte[] bytes = compound.get("pcm").getByteArray().get();
		
		this.pcm = new short[bytes.length/2];
		// to turn bytes to shorts 
		ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcm);
	}

*/
	@Override
	public int sampleRate() {
		return 44100;
	}

}
