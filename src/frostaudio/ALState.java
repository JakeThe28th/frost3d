package frostaudio;

import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.alGetListenerf;
import static org.lwjgl.openal.AL10.alListenerf;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.ALC_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.ALC_FREQUENCY;
import static org.lwjgl.openal.ALC10.ALC_REFRESH;
import static org.lwjgl.openal.ALC10.ALC_SYNC;
import static org.lwjgl.openal.ALC10.ALC_TRUE;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetInteger;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.openal.ALC11.ALC_ALL_DEVICES_SPECIFIER;
import static org.lwjgl.openal.ALC11.ALC_MONO_SOURCES;
import static org.lwjgl.openal.ALC11.ALC_STEREO_SOURCES;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.system.MemoryUtil;

import frost3d.utility.Log;

public class ALState {
	

	
	

}
