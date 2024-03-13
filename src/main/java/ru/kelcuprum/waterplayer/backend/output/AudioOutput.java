/**
 * Thanks to VSETH-GECO for this amazing audio consumer class for lavaplayer (It is kind of changed) MIT License
 * Copyright (c) 2017 VSETH-GECO Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The
 * above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ru.kelcuprum.waterplayer.backend.output;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormatTools;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.waterplayer.WaterPlayer;
import ru.kelcuprum.waterplayer.backend.MusicPlayer;

import javax.sound.sampled.*;

public class AudioOutput extends Thread {

    private final MusicPlayer musicPlayer;

    private final AudioFormat format;
    private final DataLine.Info speakerInfo;

    private Mixer mixer;
    private SourceDataLine sourceLine;

    public AudioOutput(MusicPlayer musicPlayer) {
        super("Audio Player");
        this.musicPlayer = musicPlayer;
        format = AudioDataFormatTools.toAudioFormat(musicPlayer.getAudioDataFormat());
        speakerInfo = new DataLine.Info(SourceDataLine.class, format);
        setMixer("");
    }

    @Override
    public void run() {
        try {
            final AudioPlayer player = musicPlayer.getAudioPlayer();
            final AudioDataFormat dataFormat = musicPlayer.getAudioDataFormat();

            final AudioInputStream stream = AudioPlayerInputStream.createStream(player, dataFormat, dataFormat.frameDuration(), false);

            final byte[] buffer = new byte[dataFormat.chunkSampleCount * dataFormat.channelCount * 2];
            final long frameDuration = dataFormat.frameDuration();
            int chunkSize;
            while (true) {
                if (sourceLine == null || !sourceLine.isOpen()) {
                    closeLine();
                    if (!createLine()) {
                        sleep(500);
                        continue;
                    }
                }
                if (!player.isPaused()) {
                    if ((chunkSize = stream.read(buffer)) >= 0) {
                        sourceLine.write(buffer, 0, chunkSize);
                    } else {
                        throw new IllegalStateException("Audiostream ended. This should not happen.");
                    }
                } else {
                    sourceLine.drain();
                    sleep(frameDuration);
                }
            }
        } catch (final Exception ex) {
            WaterPlayer.log(ex.getLocalizedMessage(), Level.ERROR);
        }
    }

    public void setMixer(String name) {
        if (mixer != null && mixer.getMixerInfo().getName().equals(name)) {
            return;
        }
        final Mixer oldMixer = mixer;
        mixer = findMixer(name, speakerInfo);
        closeLine();
        if (oldMixer != null) {
            if (!hasLinesOpen(oldMixer)) {
                oldMixer.close();
            }
        }
    }

    private boolean createLine() {
        if (mixer != null) {
            try {
                final SourceDataLine line = (SourceDataLine) mixer.getLine(speakerInfo);
                final AudioDataFormat dataFormat = musicPlayer.getAudioDataFormat();
                line.open(format, dataFormat.chunkSampleCount * dataFormat.channelCount * 2 * 5);
                line.start();
                sourceLine = line;
                return true;
            } catch (final LineUnavailableException ignored) {
            }
        }
        return false;
    }

    private void closeLine() {
        if (sourceLine != null) {
            sourceLine.flush();
            sourceLine.stop();
            sourceLine.close();
        }
    }

    private Mixer findMixer(String name, Line.Info lineInfo) {
        Mixer defaultMixer = null;
        for (final Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            final Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (mixer.isLineSupported(lineInfo)) {
                if (mixerInfo.getName().equals(name)) {
                    return mixer;
                }
                if (defaultMixer == null) {
                    defaultMixer = mixer;
                }
            }
        }
        return defaultMixer;
    }

    public static boolean hasLinesOpen(Mixer mixer) {
        return mixer.getSourceLines().length != 0 || mixer.getTargetLines().length != 0;
    }
}
