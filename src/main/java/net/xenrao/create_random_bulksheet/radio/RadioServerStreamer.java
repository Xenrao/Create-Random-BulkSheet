package net.xenrao.create_random_bulksheet.radio;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xenrao.create_random_bulksheet.compat.sable.SableCompatDispatcher;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RadioServerStreamer {

    public static final int CHUNK_SIZE = 16384;
    public static final double LISTEN_RADIUS = 64.0;
    private static final Map<UUID, Boolean> activeStreams = new ConcurrentHashMap<>();

    public static Path getRadioDir() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("create_random_bulksheet").resolve("radio");
        File dirFile = dir.toFile();
        if (!dirFile.exists()) dirFile.mkdirs();
        return dir;
    }

    public static List<File> getRadioFiles() {
        File dir = getRadioDir().toFile();
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".wav"));
        if (files == null || files.length == 0) return Collections.emptyList();
        Arrays.sort(files, Comparator.comparing(File::getName));
        return Arrays.asList(files);
    }

    public static void startStreaming(RadioBlockEntity be, File file, UUID streamId) {
        activeStreams.put(streamId, true);
        Thread thread = new Thread(() -> streamFile(be, file, streamId), "RadioStreamer-" + streamId);
        thread.setDaemon(true);
        thread.start();
    }

    private static void streamFile(RadioBlockEntity be, File file, UUID streamId) {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
            AudioFormat format = ais.getFormat();
            long frameLength = ais.getFrameLength();
            long totalBytes = frameLength > 0 ? frameLength * format.getFrameSize() : file.length();

            BlockPos startPos = getAudioPos(be);
            RadioStreamStartPayload startPayload = new RadioStreamStartPayload(
                    streamId, startPos, (int) format.getSampleRate(), format.getChannels(), format.getSampleSizeInBits(), totalBytes
            );
            sendToNearby(be, startPos, startPayload);

            float bytesPerSecond = format.getSampleRate() * format.getChannels() * (format.getSampleSizeInBits() / 8f);
            long sleepMs = bytesPerSecond > 0 ? (long) (CHUNK_SIZE / bytesPerSecond * 1000.0) : 50; // 1.5x hız kaldırıldı!
            if (sleepMs < 5) sleepMs = 5;
            if (sleepMs > 200) sleepMs = 200;

            byte[] buffer = new byte[CHUNK_SIZE];
            int read;
            while ((read = ais.read(buffer)) != -1) {
                if (!activeStreams.containsKey(streamId)) break;
                byte[] chunk = Arrays.copyOf(buffer, read);

                BlockPos currentAudioPos = getAudioPos(be);
                sendToNearby(be, currentAudioPos, new RadioStreamChunkPayload(streamId, currentAudioPos, chunk, false));
                Thread.sleep(sleepMs);
            }

            if (activeStreams.containsKey(streamId)) {
                BlockPos finalAudioPos = getAudioPos(be);
                sendToNearby(be, finalAudioPos, new RadioStreamChunkPayload(streamId, finalAudioPos, new byte[0], true));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            activeStreams.remove(streamId);
        }
    }

    public static void stopStreaming(RadioBlockEntity be, UUID streamId) {
        activeStreams.remove(streamId);
        BlockPos pos = getAudioPos(be);
        sendToNearby(be, pos, new RadioStopPayload(streamId, pos));
    }

    private static BlockPos getAudioPos(RadioBlockEntity be) {
        if (be.getLevel() instanceof ServerLevel serverLevel) {
            if (SableCompatDispatcher.isLoaded()) {
                GlobalPos parentGlobalPos = SableCompatDispatcher.getParentPosition(serverLevel, be.getBlockPos());
                if (parentGlobalPos != null) {
                    return parentGlobalPos.pos();
                }
            }
        }
        return be.getBlockPos();
    }

    private static void sendToNearby(RadioBlockEntity be, BlockPos audioPos, CustomPacketPayload payload) {
        if (!(be.getLevel() instanceof ServerLevel serverLevel)) return;
        BlockPos routingPos = be.getBlockPos();
        sendToPlayersInLevel(serverLevel, routingPos, audioPos, payload);
    }

    private static void sendToPlayersInLevel(ServerLevel level, BlockPos routingPos, BlockPos audioPos, CustomPacketPayload payload) {
        CustomPacketPayload actualPayload = payload;

        if (payload instanceof RadioStreamStartPayload p) {
            actualPayload = new RadioStreamStartPayload(p.streamId(), audioPos, p.sampleRate(), p.channels(), p.bitsPerSample(), p.totalDataBytes());
        } else if (payload instanceof RadioStopPayload p) {
            actualPayload = new RadioStopPayload(p.streamId(), audioPos);
        }

        PacketDistributor.sendToPlayersNear(
                level, null,
                routingPos.getX() + 0.5, routingPos.getY() + 0.5, routingPos.getZ() + 0.5,
                LISTEN_RADIUS,
                actualPayload
        );
    }
}