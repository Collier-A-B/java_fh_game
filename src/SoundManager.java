import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.awt.GraphicsEnvironment;

public class SoundManager {
    private static Map<String, byte[]> soundCache = new HashMap<>();
    private static boolean enabled = true;

    static {
        // Generate sound effects using synthesis
        generateSoundEffects();
    }

    private static void generateSoundEffects() {
        // Jump sound (rising beep)
        soundCache.put("jump", generateBeep(400, 800, 100));
        
        // Collision sound (short burst)
        soundCache.put("hit", generateNoise(100));
        
        // Power-up collect sound (ascending arpeggio)
        soundCache.put("powerup", generateArpeggio(new int[]{400, 600, 800}, 50));
        
        // Game over sound (descending tone)
        soundCache.put("gameover", generateBeep(800, 200, 500));
        
        // Score multiplier sound (short high beep)
        soundCache.put("multiplier", generateBeep(1000, 1000, 50));
    }

    private static byte[] generateBeep(double startFreq, double endFreq, int durationMs) {
        int sampleRate = 44100;
        int numSamples = (durationMs * sampleRate) / 1000;
        byte[] buffer = new byte[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            double freq = startFreq + (endFreq - startFreq) * (i / (double) numSamples);
            buffer[i] = (byte) (Math.sin(2 * Math.PI * freq * time) * 127);
        }
        
        return buffer;
    }

    private static byte[] generateNoise(int durationMs) {
        int sampleRate = 44100;
        int numSamples = (durationMs * sampleRate) / 1000;
        byte[] buffer = new byte[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            buffer[i] = (byte) (Math.random() * 127);
        }
        
        return buffer;
    }

    private static byte[] generateArpeggio(int[] frequencies, int noteDurationMs) {
        int sampleRate = 44100;
        int samplesPerNote = (noteDurationMs * sampleRate) / 1000;
        byte[] buffer = new byte[samplesPerNote * frequencies.length];
        
        for (int note = 0; note < frequencies.length; note++) {
            int freq = frequencies[note];
            for (int i = 0; i < samplesPerNote; i++) {
                double time = i / (double) sampleRate;
                buffer[note * samplesPerNote + i] = 
                    (byte) (Math.sin(2 * Math.PI * freq * time) * 127);
            }
        }
        
        return buffer;
    }

    public static void playSound(String soundName) {
        if (!enabled || GraphicsEnvironment.isHeadless()) return;
        
        try {
            byte[] soundData = soundCache.get(soundName);
            if (soundData == null) return;

            AudioFormat format = new AudioFormat(44100, 8, 1, true, true);
            AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(soundData),
                format,
                soundData.length
            );

            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            // Silently fail if sound playback fails
            enabled = false;
        }
    }

    public static void setEnabled(boolean enabled) {
        SoundManager.enabled = enabled;
    }
}
