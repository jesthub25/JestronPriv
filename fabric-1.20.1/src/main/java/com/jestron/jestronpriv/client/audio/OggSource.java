package com.jestron.jestronpriv.client.audio;

import net.minecraft.client.sounds.OggAudioStream;
import org.jcodec.common.AudioFormat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/** OGG Vorbis: oyunun kendi cozucusu (OggAudioStream) ile. */
final class OggSource {

    private OggSource() {
    }

    static AudioDecoder.Pcm decode(Path file, double max) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file)); OggAudioStream s = new OggAudioStream(in)) {
            javax.sound.sampled.AudioFormat jf = s.getFormat();
            int ch = Math.max(1, jf.getChannels());
            AudioFormat f = new AudioFormat(Math.round(jf.getSampleRate()), 16, ch, true, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
            AudioDecoder.Sink sink = new AudioDecoder.Sink(max);
            while (!sink.full()) {
                ByteBuffer b = s.read(1 << 16);
                if (b == null || !b.hasRemaining()) break;
                sink.put(b.order(ByteOrder.nativeOrder()), f);
            }
            return sink.result();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("OGG okunamadı", e);
        }
    }
}
