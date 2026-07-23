package net.xenrao.create_random_bulksheet.radio;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RadioStreamChunkPayload(
        UUID streamId,
        BlockPos pos, // Bu eklendi!
        byte[] data,
        boolean last
) implements CustomPacketPayload {

    public static final Type<RadioStreamChunkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("create_random_bulksheet", "radio_stream_chunk")
    );

    public static final StreamCodec<FriendlyByteBuf, RadioStreamChunkPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeUUID(p.streamId);
                        buf.writeBlockPos(p.pos);
                        buf.writeByteArray(p.data);
                        buf.writeBoolean(p.last);
                    },
                    buf -> new RadioStreamChunkPayload(
                            buf.readUUID(),
                            buf.readBlockPos(),
                            buf.readByteArray(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}