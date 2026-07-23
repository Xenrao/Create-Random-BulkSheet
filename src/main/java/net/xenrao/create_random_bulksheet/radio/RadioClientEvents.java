package net.xenrao.create_random_bulksheet.radio;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.xenrao.create_random_bulksheet.RandomBulkSheet;

@EventBusSubscriber(modid = RandomBulkSheet.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class RadioClientEvents {

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        RadioClientPlayer.stopAll();
    }
}