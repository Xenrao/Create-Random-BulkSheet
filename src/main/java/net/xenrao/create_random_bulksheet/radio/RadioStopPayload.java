package net.xenrao.create_random_bulksheet.radio;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RadioStopPayload(
        UUID streamId,
        BlockPos pos
) implements CustomPacketPayload {

    public static final Type<RadioStopPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("create_random_bulksheet", "radio_stop")
    );

    public static final StreamCodec<FriendlyByteBuf, RadioStopPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeUUID(p.streamId);
                        buf.writeBlockPos(p.pos);
                    },
                    buf -> new RadioStopPayload(
                            buf.readUUID(),
                            buf.readBlockPos()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}