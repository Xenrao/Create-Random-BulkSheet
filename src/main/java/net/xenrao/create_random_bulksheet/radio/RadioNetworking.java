package net.xenrao.create_random_bulksheet.radio;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;

@EventBusSubscriber(modid = RandomBulkSheet.MODID)
public class RadioNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // 1.21.1'de registrar() metodu artık bir versiyon String'i istiyor.
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                RadioStreamStartPayload.TYPE,
                RadioStreamStartPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> RadioClientPlayer.handleStart(payload))
        );

        registrar.playToClient(
                RadioStreamChunkPayload.TYPE,
                RadioStreamChunkPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> RadioClientPlayer.handleChunk(payload))
        );

        registrar.playToClient(
                RadioStopPayload.TYPE,
                RadioStopPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> RadioClientPlayer.handleStop(payload))
        );
    }
}