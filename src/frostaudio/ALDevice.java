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
import java.util.List;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.openal.EXTDisconnect;
import org.lwjgl.openal.SOFTReopenDevice;
import org.lwjgl.system.MemoryUtil;

import frost3d.utility.Log;

// For Future Reference,
// https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/openal/ALCDemo.java

public class ALDevice {
	
	static ALDevice current_device;
	
	public static void printInfo() {
		if (current_device == null) {
			Log.send("No device is currently bound");
		} else {
			Log.send("Default device: " + defaultDevice());
		    Log.send("Bound device: "	+ alcGetString(current_device.device, ALC_DEVICE_SPECIFIER));
			Log.send("OpenALC10   : " 	+ current_device.alc_capabilities.OpenALC10);
	        Log.send("OpenALC11   : " 	+ current_device.alc_capabilities.OpenALC11);
	        Log.send("ALC_EXT_EFX : " 	+ current_device.alc_capabilities.ALC_EXT_EFX);
	        Log.send("ALC_FREQUENCY      : " +  alcGetInteger(current_device.device, ALC_FREQUENCY		) + "Hz"		);
	        Log.send("ALC_REFRESH        : " +  alcGetInteger(current_device.device, ALC_REFRESH		) + "Hz"		);
	        Log.send("ALC_SYNC           : " + (alcGetInteger(current_device.device, ALC_SYNC			) == ALC_TRUE )	);
	        Log.send("ALC_MONO_SOURCES   : " +  alcGetInteger(current_device.device, ALC_MONO_SOURCES	)			  	);
	        Log.send("ALC_STEREO_SOURCES : " +  alcGetInteger(current_device.device, ALC_STEREO_SOURCES	)				);
	        Log.send("---\nOther devices:");
	        for (String name : listDevices()) Log.send("  " + name);
		}
	}
	
	/** Returns all available devices' identifiers */
	public static List<String> 	listDevices() 	{ return ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER); }
	
	/** Returns the device identifier of the default device. */
	public static String 		defaultDevice() { return alcGetString(NULL, ALC_DEFAULT_DEVICE_SPECIFIER); }
	
	/** Set the listener volume for the current context */
	public static void 			volume(float v) { alListenerf(AL_GAIN, v); }
	
	/** Get the listener volume for the current context */
	public static float 		volume() 		{ return alGetListenerf(AL_GAIN); }
	
	/** I just think this is prettier than the constructor. So, you HAVE to use it!!! */
	public static ALDevice	device(String device_name) { return new ALDevice(device_name); }
	
	/** I just think this is prettier than the constructor. So, you HAVE to use it!!! */
	public static ALDevice	preffered() { return new ALDevice(null); }
	
	// -- == Non-Static == -- //
	
	long 			device;
	long 			context;
	ALCCapabilities alc_capabilities;
	ALCapabilities  al_capabilities;
	
	/** Creates and binds a device. <br>
	 *  A null device name selects the default output device. */
	private ALDevice(String device_name) {
		device = alcOpenDevice(device_name);
		
		if (device == NULL) { 
			if (device_name == null) throw new IllegalStateException("Failed to open the default audio device ('null')"); 
			if (device_name != null) throw new IllegalStateException("Failed to open an OpenAL device." + device_name); 
		}

		alc_capabilities = ALC.createCapabilities(device);
        if (!alc_capabilities.OpenALC10) {  throw new IllegalStateException(); }

        context = alcCreateContext(device, (IntBuffer) null);
        bind();
        
		al_capabilities  = AL.createCapabilities (alc_capabilities, MemoryUtil::memCallocPointer);
	}
	
	boolean using_TLC = false;
	
	/** Make this device's context current. <br>
	 * ...I don't think the thread stuff is even relevant,
	 * considering there can only be one 'current_device'
	 * at a time. But, I might as well have the code
	 * for reference if that ever needs to be added... */
	public void bind() {
        boolean success = false;
        using_TLC = false;
        if (alc_capabilities.ALC_EXT_thread_local_context) {
        	// If this audio library context supports separate device contexts per thread,
        	// try that. (https://github.com/openalext/openalext/blob/master/ALC_EXT_thread_local_context.txt)
        	success = alcSetThreadContext(context);
        	using_TLC = true;
        } 
        if (!success) {
        	// If it doesn't, or if setting the per-thread context failed,
        	// just use the regular one current context per program version.
        	success = alcMakeContextCurrent(context);
        } 
        if (!success) {
        	// wat
        	throw new IllegalStateException(); 
        }
        current_device = this;
	}
	
	/** Destroy resources related to this audio device. */
	public void end() {
		if (current_device == this) {
			alcMakeContextCurrent(NULL);
	        if (using_TLC) {
	            AL.setCurrentThread(null);
	        } else {
	            AL.setCurrentProcess(null);
	        }
		}
        memFree(alc_capabilities.getAddressBuffer());
        memFree(al_capabilities.getAddressBuffer());
        alcDestroyContext(context);
        alcCloseDevice(device);
	}
	
	/** Returns name of the current device. */
	public String specifier() { return alcGetString(device, ALC_DEVICE_SPECIFIER); }
	
	// -- ... -- //
	
	// https://openal.org/pipermail/openal/2020-May/000751.html
	// https://openal-soft.org/openal-extensions/SOFT_reopen_device.txt
	
	/** Transfers all sources, buffers, etc created with this device 
	 *  into the context of a different device. */
	public void migrate(String new_device) {
		SOFTReopenDevice.alcReopenDeviceSOFT(device, new_device, (IntBuffer)null);
	}
	
	/** Transfers all sources, buffers, etc created with this device 
	 *  into the context of the current default device. */
	public void migrateToPreferred() {
		migrate(null);
	}
	
	// https://bugs-legacy.mojang.com/browse/MC-44055 (http://forum.lwjgl.org/index.php?topic=7081.msg37006#msg37006)
	// https://github.com/openalext/openalext/blob/master/ALC_EXT_disconnect.txt ...of course. of course it's an extension.
	
	public boolean isConnected() {
		return alcGetInteger(device, EXTDisconnect.ALC_CONNECTED) == ALC10.ALC_TRUE;
	}

}
