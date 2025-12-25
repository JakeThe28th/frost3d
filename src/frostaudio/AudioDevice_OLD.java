package frostaudio;

import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.alGetListenerf;
import static org.lwjgl.openal.AL10.alListenerf;
import static org.lwjgl.openal.ALC10.*;
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
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.system.MemoryUtil;

import frost3d.utility.Log;


/** 
 * NOTE: <br>
 * Creating multiple contexts for an audio device, or opening multiple devices at once, is not currently supported.<br>
 * Capture devices are not currently supported.
 * */
public class AudioDevice_OLD {
	
	// Static methods
	
	public static String[] availableDevices() {
		Log.send("Available devices: ");
		
        List<String> devices = ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER);
        String[] devices_as_array = new String[devices.size()];
        if (devices != null)  {
            for (int i = 0; i < devices.size(); i++) {
            	Log.send(i + ": " + devices.get(i));
            	devices_as_array[i] = devices.get(i);
            }
        } 

        return devices_as_array;
   	}
	
	/** Returns the device identifier of the default device. */
	public static String defaultDevice() { return alcGetString(NULL, ALC_DEFAULT_DEVICE_SPECIFIER); }
	// ^^^ what's the difference from ALC_DEFAULT_ALL_DEVICES_SPECIFIER ...
	
	/** Set the listener volume for the current context */
	public void setListenerVolume(float v) { alListenerf(AL_GAIN, v); }
	
	/** Get the listener volume for the current context */
	public float getListenerVolume() { return alGetListenerf(AL_GAIN); }
	
	// Non-static methods
	
	long device;  // This device's OpenAL device ID.
	long context; // This device's current context. Multiple contexts not currently supported.
	boolean use_thread_local_context;
	
	ALCCapabilities device_ALC_capabilities;
	ALCapabilities device_AL_capabilities;
	
	/**
	 * Open the provided device, and set up context appropriately.<br>
	 * Pass in null to get the default device.
	 * @param device_name
	 */
	public AudioDevice_OLD(String device_name) {
		
		device = alcOpenDevice(device_name);
		
		if (device == NULL) { 
			/* Re-assign the device name to make the error message clearer. */
			if (device_name == null) device_name = "null, aka DEFAULT";
			throw new IllegalStateException("Failed to open an OpenAL device." + device_name); 
			}

		device_ALC_capabilities = ALC.createCapabilities(device);
        
        if (!device_ALC_capabilities.OpenALC10) {  throw new IllegalStateException(); }

        System.out.println("OpenALC10  : " + device_ALC_capabilities.OpenALC10);
        System.out.println("OpenALC11  : " + device_ALC_capabilities.OpenALC11);
        System.out.println("ALC_EXT_EFX: " + device_ALC_capabilities.ALC_EXT_EFX);
		
        availableDevices();
        System.out.println("Default device: " + defaultDevice());
        System.out.println("ALC device specifier: " + thisDevice());

        context = alcCreateContext(device, (IntBuffer) null);
        bind();
		
	}
	
	/**
	 * Cleanup this audio device.
	 * You will need to re-bind other AudioDevices after calling this.
	 */
	public void end() {
		alcMakeContextCurrent(NULL);
        if (use_thread_local_context) {
            AL.setCurrentThread(null);
        } else {
            AL.setCurrentProcess(null);
        }
        memFree(device_ALC_capabilities.getAddressBuffer());

        alcDestroyContext(context);
        alcCloseDevice(device);
	}
	
	/**
	 * Make this device's context current.
	 */
	public void bind() {
	    use_thread_local_context = device_ALC_capabilities.ALC_EXT_thread_local_context && alcSetThreadContext(context);
        if (!use_thread_local_context) {
            if (!alcMakeContextCurrent(context)) { 
            	throw new IllegalStateException(); 
            }
        }

        device_AL_capabilities = AL.createCapabilities(device_ALC_capabilities, MemoryUtil::memCallocPointer);

        System.out.println("ALC_FREQUENCY     : " + alcGetInteger(device, ALC_FREQUENCY) + "Hz");
        System.out.println("ALC_REFRESH       : " + alcGetInteger(device, ALC_REFRESH) + "Hz");
        System.out.println("ALC_SYNC          : " + (alcGetInteger(device, ALC_SYNC) == ALC_TRUE));
        System.out.println("ALC_MONO_SOURCES  : " + alcGetInteger(device, ALC_MONO_SOURCES));
        System.out.println("ALC_STEREO_SOURCES: " + alcGetInteger(device, ALC_STEREO_SOURCES));
	}
	
	/** Returns the device identifier of the current device. */
	public String thisDevice() { return alcGetString(device, ALC_DEVICE_SPECIFIER); }

}
