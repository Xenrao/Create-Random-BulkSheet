package net.xenrao.create_random_bulksheet.radio;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.xenrao.create_random_bulksheet.compat.sable.SableCompatDispatcher;

import javax.sound.sampled.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@OnlyIn(Dist.CLIENT)
public class RadioClientPlayer {


    private static final float SIDE_BLEED = 0.05f;

    private static final Map<UUID, ActiveStream> activeStreams = new ConcurrentHashMap<>();

    public static void handleStart(RadioStreamStartPayload payload) {
        ActiveStream existing = activeStreams.get(payload.streamId());
        if (existing != null) {
            existing.updatePos(payload.pos());
            return;
        }

        ActiveStream stream = new ActiveStream(payload);
        activeStreams.put(payload.streamId(), stream);
        stream.start();
    }

    public static void handleChunk(RadioStreamChunkPayload payload) {
        ActiveStream stream = activeStreams.get(payload.streamId());
        if (stream != null) {
            stream.updatePos(payload.pos());
            stream.addChunk(payload.data(), payload.last());
        }
    }

    public static void handleStop(RadioStopPayload payload) {
        ActiveStream stream = activeStreams.remove(payload.streamId());
        if (stream != null) stream.stop();
    }

    public static void stopAll() {
        for (ActiveStream stream : activeStreams.values()) stream.stop();
        activeStreams.clear();
    }

    public static class ActiveStream {
        private final UUID streamId;
        private volatile BlockPos pos;
        private final AudioFormat format;
        private SourceDataLine line;
        private final Queue<byte[]> bufferQueue = new ConcurrentLinkedQueue<>();
        private volatile boolean running = false;
        private volatile boolean finished = false;
        private Thread playThread;

        private float currentLeftGain = 0f;
        private float currentRightGain = 0f;

        public ActiveStream(RadioStreamStartPayload payload) {
            this.streamId = payload.streamId();
            this.pos = payload.pos();
            this.format = new AudioFormat(payload.sampleRate(), payload.bitsPerSample(), payload.channels(), true, false);
        }

        public UUID getStreamId() { return streamId; }
        public BlockPos getPos() { return pos; }
        public void updatePos(BlockPos newPos) { this.pos = newPos; }

        public void start() {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, 4096);
                line.start();

                running = true;
                playThread = new Thread(this::playLoop, "RadioPlayer-" + streamId);
                playThread.setDaemon(true);
                playThread.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }

        private void playLoop() {
            while (running) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.isPaused() || !mc.isWindowActive()) {
                    try { Thread.sleep(50); } catch (InterruptedException e) { break; }
                    continue;
                }

                byte[] chunk = bufferQueue.poll();
                if (chunk != null && chunk.length > 0) {
                    while (bufferQueue.size() > 2) {
                        bufferQueue.poll();
                    }

                    float[] targets = calculateGains();
                    float targetLeftGain = targets[0];
                    float targetRightGain = targets[1];

                    apply3DPanning(chunk, currentLeftGain, targetLeftGain, currentRightGain, targetRightGain);
                    line.write(chunk, 0, chunk.length);

                    currentLeftGain = targetLeftGain;
                    currentRightGain = targetRightGain;
                } else if (chunk == null && finished) {
                    break;
                } else if (chunk == null) {
                    try { Thread.sleep(2); } catch (InterruptedException e) { break; }
                }
            }
            if (line != null) {
                line.drain();
                line.stop();
                line.close();
            }
            activeStreams.remove(streamId);
        }

        private float[] calculateGains() {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return new float[]{0f, 0f};

            float masterVol = mc.options.getSoundSourceVolume(SoundSource.MASTER);
            float recordVol = mc.options.getSoundSourceVolume(SoundSource.RECORDS);
            float baseVol = masterVol * recordVol;

            Vec3 listenerPos = SableCompatDispatcher.isLoaded() ? mc.gameRenderer.getMainCamera().getPosition() : player.position();
            double distance = listenerPos.distanceTo(Vec3.atCenterOf(pos));

            float maxDist = 80.0f;
            if (distance >= maxDist) return new float[]{0f, 0f};

            float ratio = (float) (0.8 - (distance / maxDist));
            baseVol *= (ratio * ratio);

            if (baseVol <= 0.001f) return new float[]{0f, 0f};

            Vec3 toSource = Vec3.atCenterOf(pos).subtract(listenerPos);
            toSource = new Vec3(toSource.x, 0, toSource.z).normalize();

            Vec3 lookVec = player.getLookAngle();
            Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();

            float pan = (float) toSource.dot(rightVec);
            pan = Math.max(-1.0f, Math.min(1.0f, pan));

            // 0.0 (Tam Sol) ile 1.0 (Tam Sağ) arasında normalize ediyoruz
            float panNormalized = (pan + 1.0f) * 0.5f;

            // SIDE_BLEED kullanılarak sağ/sol karışımı hesaplanıyor
            float leftPan = (1.0f - panNormalized) * (1.0f - SIDE_BLEED) + (panNormalized * SIDE_BLEED);
            float rightPan = panNormalized * (1.0f - SIDE_BLEED) + ((1.0f - panNormalized) * SIDE_BLEED);

            float leftVol = baseVol * leftPan;
            float rightVol = baseVol * rightPan;

            return new float[]{leftVol, rightVol};
        }

        private void apply3DPanning(byte[] chunk, float fromLeft, float toLeft, float fromRight, float toRight) {
            if (format.getSampleSizeInBits() != 16 || format.getChannels() != 2) return;
            if (chunk.length % 4 != 0) return;

            int frameCount = chunk.length / 4;
            for (int i = 0; i < frameCount; i++) {
                float lerpFactor = (float)i / frameCount;
                float leftGain = fromLeft + (toLeft - fromLeft) * lerpFactor;
                float rightGain = fromRight + (toRight - fromRight) * lerpFactor;

                int offset = i * 4;
                short leftSample = (short) ((chunk[offset] & 0xFF) | (chunk[offset + 1] << 8));
                leftSample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, leftSample * leftGain));
                chunk[offset] = (byte) (leftSample & 0xFF);
                chunk[offset + 1] = (byte) ((leftSample >> 8) & 0xFF);

                short rightSample = (short) ((chunk[offset + 2] & 0xFF) | (chunk[offset + 3] << 8));
                rightSample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, rightSample * rightGain));
                chunk[offset + 2] = (byte) (rightSample & 0xFF);
                chunk[offset + 3] = (byte) ((rightSample >> 8) & 0xFF);
            }
        }

        public void addChunk(byte[] data, boolean last) {
            if (data.length > 0) bufferQueue.add(data);
            if (last) finished = true;
        }

        public void stop() {
            running = false;
            finished = true;
            if (line != null) {
                try {
                    line.stop();
                    line.flush();
                    line.close();
                } catch (Exception ignored) {}
            }
        }
    }
}