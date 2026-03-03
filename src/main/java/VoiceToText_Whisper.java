import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;

public class VoiceToText_Whisper {
    private static final int SAMPLE_RATE = 16000;
    private static final int SILENCE_THRESHOLD = 500; // silence level
    private static final int SILENCE_DURATION_MS = 3000; // 3 seconds of silence to stop
    private static final int CHUNK_SIZE = SAMPLE_RATE * 2; // 1 second of 16-bit audio
    private WhisperJNI whisper;
    private WhisperContext ctx;
    private WhisperFullParams params;

    String modelPath = "src/Resources/ggml-base.bin";

    public VoiceToText_Whisper() {
        try {
            WhisperJNI.loadLibrary();
        } catch (IOException e) {
            System.out.println(e);
        }
        WhisperJNI.setLibraryLogger(null);
        whisper = new WhisperJNI();
        try {
            ctx = whisper.init(Path.of(modelPath));
        } catch (Exception ee) {
            System.err.println(ee);
        }
        params = new WhisperFullParams();
        params.language = "en";
        params.nThreads = Runtime.getRuntime().availableProcessors();
        params.noContext = false;
        params.printSpecial = false;

    }

    public String GetVoiceInput() {
        System.out.println("Listening... (will stop after 3 seconds of silence)");
        byte[] audioData = recordAudio();
        if (audioData.length > 0) {
            System.out.println("Processing...");
            float[] samples = bytesToFloats(audioData);
            return transcribeAndPrint(whisper, modelPath, samples);
        } else {
            System.out.println("No audio recorded.");
        }
        return "ERROR";
    }

    private static byte[] recordAudio() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

        try {
            TargetDataLine mic = (TargetDataLine) AudioSystem
                    .getLine(new DataLine.Info(TargetDataLine.class, format));
            mic.open(format);
            mic.start();

            ByteBuffer audioBuffer = ByteBuffer.allocate(SAMPLE_RATE * 60 * 2); // Max 60 seconds
            audioBuffer.order(ByteOrder.LITTLE_ENDIAN);

            byte[] buffer = new byte[CHUNK_SIZE];
            long lastSoundTime = System.currentTimeMillis();
            boolean soundDetected = false;

            while (true) {
                int bytesRead = mic.read(buffer, 0, buffer.length);

                if (bytesRead > 0) {
                    audioBuffer.put(buffer, 0, bytesRead);

                    // Check if this chunk has sound
                    if (hasSoundActivity(buffer, bytesRead)) {
                        lastSoundTime = System.currentTimeMillis();
                        soundDetected = true;
                    }

                    // Check for silence timeout (only after sound was detected)
                    if (soundDetected && System.currentTimeMillis() - lastSoundTime > SILENCE_DURATION_MS) {
                        mic.stop();
                        mic.close();
                        break;
                    }
                }
            }

            audioBuffer.flip();
            byte[] result = new byte[audioBuffer.remaining()];
            audioBuffer.get(result);
            return result;

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    private static boolean hasSoundActivity(byte[] buffer, int len) {
        // Simple RMS-based sound detection
        long sum = 0;
        for (int i = 0; i < len; i += 2) {
            int low = buffer[i] & 0xff;
            int high = buffer[i + 1];
            short sample = (short) ((high << 8) | low);
            sum += sample * sample;
        }
        double rms = Math.sqrt((double) sum / (len / 2));
        return rms > SILENCE_THRESHOLD;
    }

    private String transcribeAndPrint(WhisperJNI whisper, String modelPath, float[] samples) {
        // try {
        // var params = new WhisperFullParams();
        // params.language = "en";
        // params.nThreads = 8;
        // params.translate = false;
        // params.printSpecial = false;
        // params.noContext = false; // VERY IMPORTANT
        // var ctx = whisper.init(Path.of(modelPath));
        int result = whisper.full(ctx, params, samples, samples.length);

        if (result != 0) {
            System.out.println("Transcription failed with code " + result);
            return "ERROR";
        }

        int numSegments = whisper.fullNSegments(ctx);
        StringBuilder fullText = new StringBuilder();

        for (int i = 0; i < numSegments; i++) {
            String text = whisper.fullGetSegmentText(ctx, i);
            fullText.append(text);
        }

        return fullText.toString().replaceAll("\\s+", " ").strip();

    }

    private static float[] bytesToFloats(byte[] audioBytes) {
        int samples = audioBytes.length / 2;
        float[] floatData = new float[samples];
        for (int i = 0; i < samples; i++) {
            int low = audioBytes[i * 2] & 0xff;
            int high = audioBytes[i * 2 + 1];
            short sample = (short) ((high << 8) | low);
            floatData[i] = sample / 32768.0f;
        }
        return floatData;
    }
}
