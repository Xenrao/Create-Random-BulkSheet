package net.xenrao.create_random_bulksheet.radio;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RadioStreamStartPayload(
        UUID streamId,
        BlockPos pos,
        int sampleRate,
        int channels,
        int bitsPerSample,
        long totalDataBytes
) implements CustomPacketPayload {

    public static final Type<RadioStreamStartPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("create_random_bulksheet", "radio_stream_start")
    );

    public static final StreamCodec<FriendlyByteBuf, RadioStreamStartPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeUUID(p.streamId);
                        buf.writeBlockPos(p.pos);
                        buf.writeInt(p.sampleRate);
                        buf.writeInt(p.channels);
                        buf.writeInt(p.bitsPerSample);
                        buf.writeLong(p.totalDataBytes);
                    },
                    buf -> new RadioStreamStartPayload(
                            buf.readUUID(),
                            buf.readBlockPos(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readInt(),
                            buf.readLong()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}