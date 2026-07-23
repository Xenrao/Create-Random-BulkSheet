package net.xenrao.create_random_bulksheet.radio;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class RadioBlockEntity extends BlockEntity {

    private UUID streamId;
    private boolean playing;
    private int currentTrackIndex = 0;

    public RadioBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void togglePlay(Player player) {
        if (playing) {
            stop();
            player.sendSystemMessage(Component.literal("\u00a77[Radio] \u00a7cDurduruldu"));
        } else {
            play(player);
        }
    }

    // direction: 1 = Sonraki, -1 = Önceki
    public void cycleTrack(Player player, int direction) {
        List<File> files = RadioServerStreamer.getRadioFiles();
        if (files.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "\u00a77[Radio] \u00a7cconfig/create_random_bulksheet/radio/ klasorunde WAV yok!"));
            return;
        }

        int size = files.size();
        // Modüler aritmetik ile liste başına/sonuna gelince döngüyü sağla
        currentTrackIndex = (currentTrackIndex + direction + size) % size;

        player.sendSystemMessage(Component.literal(
                "\u00a77[Radio] \u00a7aSecili parca: \u00a7f" + files.get(currentTrackIndex).getName()));

        if (playing) {
            stop();
            play(player);
        }
    }

    public void play(Player player) {
        if (level == null || level.isClientSide()) return;

        if (streamId != null) {
            RadioServerStreamer.stopStreaming(this, streamId);
            streamId = null;
        }

        List<File> files = RadioServerStreamer.getRadioFiles();
        if (files.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "\u00a77[Radio] \u00a7cconfig/create_random_bulksheet/radio/ klasorunde WAV yok!"));
            return;
        }

        File wavFile = files.get(currentTrackIndex % files.size());
        player.sendSystemMessage(Component.literal(
                "\u00a77[Radio] \u00a7aCaliniyor: \u00a7f" + wavFile.getName()));

        UUID id = UUID.randomUUID();
        this.streamId = id;
        this.playing = true;
        setChanged();
        RadioServerStreamer.startStreaming(this, wavFile, id);
    }

    public void stop() {
        if (level == null) return;
        if (!level.isClientSide() && streamId != null) {
            RadioServerStreamer.stopStreaming(this, streamId);
        }
        playing = false;
        streamId = null;
        setChanged();
    }

    public UUID getStreamId() {
        return streamId;
    }

    public void setStreamId(UUID id) {
        this.streamId = id;
        setChanged();
    }

    @Override
    public void setRemoved() {
        stop();
        super.setRemoved();
    }
}