package brova.toshiba.client.utils.audio;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class SoundUtil {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(input -> {
        Thread thread = new Thread(input, "toshiba-ClientSounds");
        thread.setDaemon(true);
        return thread;
    });

    public static void playSound(String text, double doubleValue, float floatValue) {
        executorService.execute(() -> updateResourceAsStreamWithAudioInputStream(text, doubleValue, floatValue));
    }

    private static void updateResourceAsStreamWithAudioInputStream(String text, double doubleValue, float floatValue) {
        String textValue = "/assets/toshiba/sounds/" + text;
        try (InputStream resourceAsStream = SoundUtil.class.getResourceAsStream(textValue)) {
            if (resourceAsStream != null) {
                try (BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream);
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
                    AudioInputStream audioInputStreamValue = createAudioInputStreamFromFormatWithAllBytes(audioInputStream, floatValue)) {
                    Clip clip = AudioSystem.getClip();
                    clip.addLineListener(input -> {
                        if (input.getType() == Type.STOP) {
                            clip.close();
                        }
                    });
                    clip.open(audioInputStreamValue);
                    updateControlSupportedWithControl(clip, doubleValue);
                    clip.start();
                }
            }
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException failure) {
            System.err.println("[SoundUtil] " + failure.getMessage());
        }
    }

    private static AudioInputStream createAudioInputStreamFromFormatWithAllBytes(AudioInputStream audioInputStream, float floatValue) throws IOException {
        if (floatValue == 1.0F) {
            return audioInputStream;
        } else {
            AudioFormat format = audioInputStream.getFormat();
            byte[] values = audioInputStream.readAllBytes();
            float floatValueValue = format.getSampleRate() * Math.max(0.5F, Math.min(2.0F, floatValue));
            AudioFormat audioFormat = new AudioFormat(floatValueValue, format.getSampleSizeInBits(), format.getChannels(), true, format.isBigEndian());
            return new AudioInputStream(new ByteArrayInputStream(values), audioFormat, values.length / audioFormat.getFrameSize());
        }
    }

    private static void updateControlSupportedWithControl(Clip clip, double doubleValue) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            double doubleValueValue = Math.max(0.0, Math.min(1.0, doubleValue));
            FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float floatValue = (float) (Math.log10(doubleValueValue <= 0.0 ? 1.0E-4 : doubleValueValue) * 20.0);
            control.setValue(floatValue);
        }
    }

    private SoundUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
