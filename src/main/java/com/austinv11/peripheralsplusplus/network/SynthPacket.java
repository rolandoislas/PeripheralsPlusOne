package com.austinv11.peripheralsplusplus.network;

import com.austinv11.peripheralsplusplus.PeripheralsPlusPlus;
import com.austinv11.peripheralsplusplus.reference.Config;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.voicerss.tts.AudioCodec;
import com.voicerss.tts.AudioFormat;
import com.voicerss.tts.VoiceParameters;
import com.voicerss.tts.VoiceProvider;
import dan200.computercraft.api.turtle.TurtleSide;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.UUID;

public class SynthPacket implements IMessage {

	private String apiKey;
	private boolean useWebService;
	private UUID eventId;
	private BlockPos pos;
    public String text;
	public String voice;
	public Float pitch;
	public Float pitchRange;
	public Float pitchShift;
	public Float rate;
	public Float volume;
	public TurtleSide side;
	
	public SynthPacket() {
		
	}
	
	public SynthPacket(String text, String voice, Float pitch, Float pitchRange, Float pitchShift, Float rate, Float volume,
					   BlockPos pos, int world, TurtleSide side, UUID eventId, boolean useWebService, String apiKey) {
		this.text = text;
		this.voice = voice;
		this.pitch = pitch;
		this.pitchRange = pitchRange;
		this.pitchShift = pitchShift;
		this.rate = rate;
		this.volume = volume;
		this.pos = pos;
		this.side = side;
		this.eventId = eventId;
		this.useWebService = useWebService;
		this.apiKey = apiKey;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		text = tag.getString("text");
		voice = tag.getString("voice");
		pitch = tag.getString("pitch").equals("null") ? null : Float.parseFloat(tag.getString("pitch"));
		pitchRange = tag.getString("pitchRange").equals("null") ? null : Float.parseFloat(tag.getString("pitchRange"));
		pitchShift = tag.getString("pitchShift").equals("null") ? null : Float.parseFloat(tag.getString("pitchShift"));
		rate = tag.getString("pitch").equals("null") ? null : Float.parseFloat(tag.getString("pitch"));
		volume = tag.getString("volume").equals("null") ? null : Float.parseFloat(tag.getString("volume"));
        int[] posArray = tag.getIntArray("pos");
        pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
		side = tag.getString("side").equals("null") ? null : TurtleSide.valueOf(tag.getString("side"));
		eventId = tag.getUniqueId("eventId");
		useWebService = tag.getBoolean("useWebService");
		apiKey = tag.getString("apiKey");
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("text", text);
		tag.setString("voice", voice);
		tag.setString("pitch", pitch == null ? "null" : pitch.toString());
		tag.setString("pitchRange", pitchRange == null ? "null" : pitchRange.toString());
		tag.setString("pitchShift", pitchShift == null ? "null" : pitchShift.toString());
		tag.setString("rate", rate == null ? "null" : rate.toString());
		tag.setString("volume", volume == null ? "null" : volume.toString());
        tag.setIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
		tag.setString("side", side == null ? "null" : side.name());
		tag.setUniqueId("eventId", eventId);
		tag.setBoolean("useWebService", useWebService);
		tag.setString("apiKey", apiKey);
		ByteBufUtils.writeTag(buf, tag);
	}
	
	public static class SynthPacketHandler implements IMessageHandler<SynthPacket, IMessage> { //TODO: Use SimpleRunnable
		
		@Override
		public IMessage onMessage(SynthPacket message, MessageContext ctx) {
			new Thread(new SynthThread(message), Reference.MOD_NAME+" Synth Thread").start();
			return null;
		}
		
		private class SynthThread implements Runnable {
			
			private SynthPacket message;
			
			public SynthThread(SynthPacket message) {
				this.message = message;
			}
			
			@Override
			public void run() {
				boolean success = false;
				String errorMessage = "";
				// FreeTTS
				if (!message.useWebService) {
					System.setProperty("mbrola.base", Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath(),
							"mods/peripheralsplusone/mbrola").toFile().getAbsolutePath());
					System.setProperty("freetts.voices",
							"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory," +
									"de.dfki.lt.freetts.en.us.MbrolaVoiceDirectory");

					try {
						Voice voice = VoiceManager.getInstance().getVoice(message.voice);
						if (voice != null) {
							voice.allocate();
							if (message.pitch != null)
								voice.setPitch(message.pitch);
							if (message.pitchRange != null)
								voice.setPitchRange(message.pitchRange);
							if (message.pitchShift != null)
								voice.setPitchShift(message.pitchShift);
							if (message.rate != null)
								voice.setRate(message.rate);
							if (message.volume != null)
								voice.setVolume(message.volume);
							success = voice.speak(message.text);
							voice.deallocate();
						}
					} catch (Exception e) {
						e.printStackTrace();
						errorMessage = e.getMessage();
					}
				}
				// Voice RSS
				else {
					String apiKey = message.apiKey;
					if (apiKey == null || apiKey.isEmpty())
						apiKey = Config.voiceRssApiKey;
					if (apiKey != null && !apiKey.isEmpty() && message.text != null && !message.text.isEmpty()
							&& message.voice != null) {
						VoiceProvider voiceProvider = new VoiceProvider(apiKey);
						VoiceParameters parameters = new VoiceParameters(message.text, message.voice);
						parameters.setCodec(AudioCodec.WAV);
						parameters.setFormat(AudioFormat.Format_44KHZ.AF_44khz_16bit_stereo);
						parameters.setBase64(false);
						parameters.setSSML(false);
						if (message.rate != null)
							parameters.setRate(message.rate.intValue());
						try {
							byte[] voice = voiceProvider.speech(parameters);
							WaitLineListener lineListener = new WaitLineListener();
							try (AudioInputStream inputStream =
										 AudioSystem.getAudioInputStream(new ByteArrayInputStream(voice))) {
								try (Clip clip = (Clip) AudioSystem.getLine(
										new DataLine.Info(Clip.class, inputStream.getFormat()))) {
									clip.addLineListener(lineListener);
									clip.open(inputStream);
									if (message.volume != null &&
											clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
										((FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN))
												.setValue(message.volume);
									clip.start();
									lineListener.waitUntilDone();
									success = true;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							errorMessage = e.getMessage();
						}
					}
				}
				synchronized (this) {
					PeripheralsPlusPlus.NETWORK.sendToServer(new SynthResponsePacket(message.text, message.pos,
							Minecraft.getMinecraft().world, message.side, message.eventId, success, errorMessage));
				}
			}

			private class WaitLineListener implements LineListener {
				private boolean done = false;

				@Override
				public synchronized void  update(LineEvent event) {
					if (event.getType().equals(LineEvent.Type.STOP) || event.getType().equals(LineEvent.Type.CLOSE)) {
						done = true;
						notifyAll();
					}
				}

				synchronized void waitUntilDone() throws InterruptedException {
					while (!done)
						wait();
				}
			}
		}
	}
}
