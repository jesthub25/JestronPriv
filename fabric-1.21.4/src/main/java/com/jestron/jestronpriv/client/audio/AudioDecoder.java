package com.jestron.jestronpriv.client.audio;

import org.jcodec.codecs.mpa.Mp3Decoder;
import org.jcodec.common.AudioFormat;
import org.jcodec.common.Codec;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.AudioBuffer;
import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp3.MPEGAudioDemuxer;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;

/**
 * Cihazdaki ses / video dosyasindan ses cozer (saf Java): MP4 / M4A / MOV / 3GP (AAC), MP3, WAV. OGG oyunun kendi
 * cozucusuyla ({@link OggSource}). Cikti: 16 bit, en fazla 2 kanal, en fazla {@code maxSeconds} saniye.
 * MP4 / MP3 cozumu JCodec (BSD) ve JAAD (kamu malı) kodunu kullanir; bkz. THIRD_PARTY_NOTICES.
 */
public final class AudioDecoder {

    /** Cozulmus ses: ic ice (interleaved) 16 bit ornekler. */
    public static final class Pcm {
        public final short[] data;
        public final int channels, rate;

        public Pcm(short[] data, int channels, int rate) {
            this.data = data;
            this.channels = channels;
            this.rate = rate;
        }

        public int frames() {
            return data.length / Math.max(1, channels);
        }

        public double seconds() {
            return frames() / (double) Math.max(1, rate);
        }
    }

    /** Desteklenen uzantilar. */
    public static final String[] EXTENSIONS = {"mp4", "m4a", "m4b", "mov", "3gp", "mp3", "wav", "ogg", "oga"};

    /** Dosya boyutu siniri (video dosyalari buyuk olabilir; sadece ses izi okunur). */
    public static final long MAX_FILE = 2_000_000_000L;

    static {
        // kutuphane gunlugu: sadece hatalar (oyun gunlugunu doldurmasin)
        org.jcodec.common.logging.Logger.setLevel(org.jcodec.common.logging.LogLevel.ERROR);
    }

    private AudioDecoder() {
    }

    public static String ext(Path p) {
        String n = p.getFileName().toString();
        int i = n.lastIndexOf('.');
        return i < 0 ? "" : n.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    public static boolean supported(Path p) {
        String e = ext(p);
        for (String s : EXTENSIONS) if (s.equals(e)) return true;
        return false;
    }

    public static Pcm decode(Path file, double maxSeconds) throws IOException {
        if (file == null || !Files.isRegularFile(file)) throw new IOException("Dosya yok");
        long size = Files.size(file);
        if (size <= 0) throw new IOException("Dosya boş");
        if (size > MAX_FILE) throw new IOException("Dosya çok büyük");
        double max = Math.max(0.5, Math.min(600, maxSeconds));
        String e = ext(file);
        return switch (e) {
            case "mp3" -> mp3(file.toFile(), max);
            case "wav" -> wav(file, max);
            case "ogg", "oga" -> OggSource.decode(file, max);
            case "mp4", "m4a", "m4b", "mov", "3gp" -> mp4(file.toFile(), max);
            default -> throw new IOException("Desteklenmeyen biçim: ." + e);
        };
    }

    // ------------------------------------------------------------------ toplayici

    /** Ornekleri buyuyen diziye toplar; kanal / hiz ilk bloktan alinir, en fazla 2 kanal. */
    static final class Sink {
        short[] buf = new short[1 << 16];
        int n, channels = -1, rate = -1;
        final double maxSeconds;

        Sink(double maxSeconds) {
            this.maxSeconds = maxSeconds;
        }

        boolean full() {
            return channels > 0 && rate > 0 && n / (double) (channels * rate) >= maxSeconds;
        }

        void ensure(int extra) {
            if (n + extra <= buf.length) return;
            long want = Math.max((long) buf.length * 2, (long) n + extra);
            if (want > Integer.MAX_VALUE - 8) throw new IllegalStateException("ses çok uzun");
            short[] nb = new short[(int) want];
            System.arraycopy(buf, 0, nb, 0, n);
            buf = nb;
        }

        /** 16 bit (ya da 24/32) PCM blogu. */
        void put(ByteBuffer data, AudioFormat f) {
            if (f == null || data == null) return;
            int ch = Math.max(1, f.getChannels()), bits = f.getSampleSizeInBits(), bytes = Math.max(1, bits / 8);
            if (channels < 0) {
                channels = Math.min(2, ch);
                rate = f.getSampleRate();
            }
            if (Math.min(2, ch) != channels || f.getSampleRate() != rate) return; // ortada bicim degisirse yok say
            ByteBuffer d = data.duplicate().order(f.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            int frames = d.remaining() / (bytes * ch);
            ensure(frames * channels);
            for (int i = 0; i < frames; i++) {
                int base = d.position();
                for (int c = 0; c < channels; c++) {
                    int src = ch == 1 ? 0 : c;
                    buf[n++] = sample(d, base + src * bytes, bits, f.isSigned());
                }
                d.position(base + ch * bytes);
            }
        }

        /** JAAD ciktisi: kanal-duzlemsel 16 bit. */
        void putPlanar(ByteBuffer data, AudioFormat f) {
            if (f == null || data == null) return;
            int ch = Math.max(1, f.getChannels());
            if (channels < 0) {
                channels = Math.min(2, ch);
                rate = f.getSampleRate();
            }
            if (Math.min(2, ch) != channels || f.getSampleRate() != rate) return;
            ByteBuffer d = data.duplicate().order(f.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            int total = d.remaining() / 2, len = total / ch;
            int base = d.position();
            ensure(len * channels);
            for (int i = 0; i < len; i++) {
                for (int c = 0; c < channels; c++) buf[n++] = d.getShort(base + (c * len + i) * 2);
            }
        }

        Pcm result() throws IOException {
            if (n == 0 || channels <= 0 || rate <= 0) throw new IOException("Ses izi bulunamadı");
            int keep = (int) Math.min(n, (long) (maxSeconds * rate) * channels);
            keep -= keep % channels;
            short[] out = new short[keep];
            System.arraycopy(buf, 0, out, 0, keep);
            return new Pcm(out, channels, rate);
        }
    }

    static short sample(ByteBuffer d, int at, int bits, boolean signed) {
        switch (bits) {
            case 8: {
                int v = d.get(at) & 0xFF;
                return (short) (signed ? (byte) v << 8 : (v - 128) << 8);
            }
            case 24: {
                int b0, b1, b2;
                if (d.order() == ByteOrder.LITTLE_ENDIAN) {
                    b0 = d.get(at) & 0xFF;
                    b1 = d.get(at + 1) & 0xFF;
                    b2 = d.get(at + 2);
                } else {
                    b2 = d.get(at);
                    b1 = d.get(at + 1) & 0xFF;
                    b0 = d.get(at + 2) & 0xFF;
                }
                return (short) (((b2 << 16) | (b1 << 8) | b0) >> 8);
            }
            case 32:
                return (short) (d.getInt(at) >> 16);
            default:
                return d.getShort(at);
        }
    }

    // ------------------------------------------------------------------ MP4 (AAC)

    static Pcm mp4(File f, double max) throws IOException {
        try (FileChannelWrapper ch = NIOUtils.readableChannel(f)) {
            MP4Demuxer dm = MP4Demuxer.createMP4Demuxer(ch);
            List<DemuxerTrack> tracks = dm.getAudioTracks();
            DemuxerTrack track = null;
            DemuxerTrackMeta meta = null;
            for (DemuxerTrack t : tracks) {
                DemuxerTrackMeta m = t.getMeta();
                if (m != null && m.getCodec() == Codec.AAC) {
                    track = t;
                    meta = m;
                    break;
                }
            }
            if (track == null) throw new IOException(tracks.isEmpty() ? "Dosyada ses yok" : "Ses AAC değil (desteklenmiyor)");
            ByteBuffer priv = meta.getCodecPrivate();
            if (priv == null) throw new IOException("AAC ayarları eksik");
            // JAAD dogrudan: cikti kanal-duzlemsel (once tum sol, sonra tum sag), cevrilmis tampon olarak doner
            net.sourceforge.jaad.aac.Decoder dec = new net.sourceforge.jaad.aac.Decoder(priv.duplicate());
            Sink sink = new Sink(max);
            ByteBuffer dst = ByteBuffer.allocate(1 << 18).order(ByteOrder.LITTLE_ENDIAN);
            Packet p;
            int bad = 0, good = 0;
            while (!sink.full() && (p = track.nextFrame()) != null) {
                dst.clear();
                try {
                    // JCodec MP4 izi AAC karelerini ADTS basligiyla sarar: basligi atla
                    ByteBuffer frame = p.getData();
                    org.jcodec.codecs.aac.ADTSParser.read(frame);
                    ByteBuffer out = dec.decodeFrame(frame, dst);
                    if (out == null) continue;
                    sink.putPlanar(out, dec.getMeta());
                    good++;
                } catch (RuntimeException | IOException ex) {
                    if (++bad > 50 && good == 0) throw new IOException("AAC çözülemedi", ex);
                }
            }
            return sink.result();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("MP4 okunamadı", e);
        }
    }

    // ------------------------------------------------------------------ MP3

    static Pcm mp3(File f, double max) throws IOException {
        try (FileChannelWrapper ch = NIOUtils.readableChannel(f)) {
            MPEGAudioDemuxer dm = new MPEGAudioDemuxer(ch);
            Mp3Decoder dec = new Mp3Decoder();
            Sink sink = new Sink(max);
            ByteBuffer dst = ByteBuffer.allocate(1 << 16);
            AudioFormat fmt = null;
            Packet p;
            int bad = 0, good = 0;
            while (!sink.full() && (p = dm.nextFrame()) != null) {
                dst.clear();
                try {
                    ByteBuffer frame = p.getData();
                    if (fmt == null) fmt = dec.getCodecMeta(frame.duplicate()).getFormat();
                    AudioBuffer ab = dec.decodeFrame(frame, dst);
                    sink.put(ab.getData(), ab.getFormat() != null ? ab.getFormat() : fmt);
                    good++;
                } catch (RuntimeException | IOException ex) {
                    if (++bad > 50 && good == 0) throw new IOException("MP3 çözülemedi", ex);
                }
            }
            return sink.result();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("MP3 okunamadı", e);
        }
    }

    // ------------------------------------------------------------------ WAV (kendi RIFF okuyucumuz)

    private static final int RIFF = 0x46464952, WAVE = 0x45564157, FMT = 0x20746D66, DATA = 0x61746164;

    /** WAV: PCM 8 / 16 / 24 / 32 bit tamsayi ve 32 / 64 bit kayan nokta (WAVE_FORMAT_EXTENSIBLE dahil). */
    static Pcm wav(Path file, double max) throws IOException {
        try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
            long fileSize = fc.size();
            ByteBuffer h = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
            readFully(fc, h);
            if (h.getInt(0) != RIFF || h.getInt(8) != WAVE) throw new IOException("Geçerli bir WAV dosyası değil");
            int tag = -1, ch = 0, rate = 0, bits = 0, align = 0;
            ByteBuffer ck = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            while (fc.position() + 8 <= fileSize) {
                ck.clear();
                readFully(fc, ck);
                int id = ck.getInt(0);
                long size = ck.getInt(4) & 0xFFFFFFFFL;
                long body = fc.position();
                if (id == FMT) {
                    if (size < 16) throw new IOException("WAV başlığı bozuk");
                    ByteBuffer f = ByteBuffer.allocate((int) Math.min(size, 64)).order(ByteOrder.LITTLE_ENDIAN);
                    readFully(fc, f);
                    tag = f.getShort(0) & 0xFFFF;
                    ch = f.getShort(2) & 0xFFFF;
                    rate = f.getInt(4);
                    align = f.getShort(12) & 0xFFFF;
                    bits = f.getShort(14) & 0xFFFF;
                    if (tag == 0xFFFE && f.capacity() >= 26) tag = f.getShort(24) & 0xFFFF;
                } else if (id == DATA) {
                    if (tag < 0) throw new IOException("WAV biçim bilgisi yok");
                    long avail = fileSize - body;
                    long len = size == 0 || size == 0xFFFFFFFFL || size > avail ? avail : size;
                    return wavData(fc, len, tag, ch, rate, bits, align, max);
                }
                long next = body + size + (size & 1);
                if (next <= body || next > fileSize) break;
                fc.position(next);
            }
            throw new IOException("WAV içinde ses verisi yok");
        }
    }

    private static Pcm wavData(FileChannel fc, long len, int tag, int ch, int rate, int bits, int align, double max) throws IOException {
        if (ch < 1 || ch > 32 || rate < 1000 || rate > 384000) throw new IOException("WAV biçimi desteklenmiyor");
        int bytes = align > 0 && align % ch == 0 ? align / ch : (bits + 7) / 8;
        boolean flt = tag == 3;
        if (tag != 1 && !flt) throw new IOException("Sıkıştırılmış WAV desteklenmiyor (PCM olarak kaydet)");
        if (flt ? bytes != 4 && bytes != 8 : bytes < 1 || bytes > 4) throw new IOException("WAV bit derinliği desteklenmiyor");
        int frameBytes = bytes * ch, outCh = Math.min(2, ch);
        Sink sink = new Sink(max);
        ByteBuffer in = ByteBuffer.allocate(frameBytes * 4096).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer conv = ByteBuffer.allocate(outCh * 2 * 4096).order(ByteOrder.LITTLE_ENDIAN);
        AudioFormat f16 = new AudioFormat(rate, 16, outCh, true, false);
        AudioFormat raw = new AudioFormat(rate, bytes * 8, ch, bytes > 1, false);
        long left = len - len % frameBytes;
        while (left > 0 && !sink.full()) {
            in.clear();
            if (left < in.capacity()) in.limit((int) left);
            int got = 0;
            while (in.hasRemaining()) {
                int r = fc.read(in);
                if (r < 0) break;
                got += r;
            }
            int usable = got - got % frameBytes;
            if (usable <= 0) break;
            left -= got;
            in.flip();
            in.limit(usable);
            if (!flt) {
                sink.put(in, raw);
                continue;
            }
            conv.clear();
            int frames = usable / frameBytes;
            for (int i = 0; i < frames; i++) {
                int b = i * frameBytes;
                for (int c = 0; c < outCh; c++) {
                    int src = ch == 1 ? 0 : c;
                    double v = bytes == 4 ? in.getFloat(b + src * 4) : in.getDouble(b + src * 8);
                    if (!(v == v)) v = 0;   // NaN
                    conv.putShort((short) Math.round(Math.max(-1.0, Math.min(1.0, v)) * 32767.0));
                }
            }
            conv.flip();
            sink.put(conv, f16);
        }
        return sink.result();
    }

    private static void readFully(FileChannel fc, ByteBuffer b) throws IOException {
        while (b.hasRemaining()) if (fc.read(b) < 0) throw new IOException("Dosya beklenmedik şekilde bitti");
    }
}
