package net.xenrao.create_random_bulksheet.blocks.delayed_transporter;

import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.compat.sable.SableCompatDispatcher;

import java.util.EnumMap;
import java.util.List;

public class DelayedTransporterBlockEntity extends SmartBlockEntity {

    protected final ItemStackHandler inventory = new ItemStackHandler(1);
    private VersionedInventoryTrackerBehaviour invTracker;
    private ScrollValueBehaviour delay;

    // Üst/alt komşu capability'lerini cache'liyoruz - her tick yeniden lookup yapmamak için
    private final EnumMap<Direction, BlockCapabilityCache<IItemHandler, Direction>> capCaches = new EnumMap<>(Direction.class);

    public DelayedTransporterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private Direction inputDir() {
        return ThresholdSwitchBlock.getTargetDirection(getBlockState()).getOpposite();
    }

    private Direction outputDir() {
        return ThresholdSwitchBlock.getTargetDirection(getBlockState());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RandomBulkSheetBlockEntities.DELAYED_TRANSPORTER.get(),
                (be, side) -> be.inventory
        );
    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        invTracker = new VersionedInventoryTrackerBehaviour(this);
        behaviours.add(invTracker);

        delay = new ScrollValueBehaviour(
                Component.translatable("create_random_bulksheet.delayed_transporter.delay"),
                this,
                new ValueBoxTransform.Sided() {
                    @Override
                    protected Vec3 getSouthLocation() { return new Vec3(0.5, 0.5, 0.97); }
                    @Override
                    protected boolean isSideActive(BlockState state, Direction direction) {
                        return direction != inputDir() && direction != outputDir();
                    }
                }
        ) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, 99, 10,
                        CreateLang.translatedOptions("generic.unit", "ticks", "seconds"),
                        new ValueSettingsFormatter(this::formatSettings));
            }

            @Override
            public ValueSettings getValueSettings() {
                int val = Math.max(5, this.value);
                int row = (val >= 100) ? 1 : 0;
                int displayVal = (row == 1) ? (val / 20) : val;
                return new ValueSettings(row, Math.max(5, displayVal));
            }

            @Override
            public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
                int val = settings.value();
                if (settings.row() == 1) {
                    val = Math.max(5, val);
                    setValue(Math.min(2000, val * 20));
                } else {
                    val = Math.max(5, val);
                    setValue(Math.min(2000, val));
                }
                playFeedbackSound(this);
            }

            public MutableComponent formatSettings(ValueSettings settings) {
                int v = settings.value();
                return Component.literal(settings.row() == 1 ? Math.max(5, v) + "s" : Math.max(5, v) + "t");
            }
        };

        delay.between(5, 2000);
        delay.setValue(10);
        delay.withFormatter(this::format);
        delay.withCallback(this::ondelayChanged);
        behaviours.add(delay);
    }
    private void ondelayChanged(int newValue) {
        setLazyTickRate(Math.max(1, newValue));
    }

    private String format(int value) {
        if (value < 100) return value + "t";
        return (value / 20) + "s";
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (delay != null) {
            setLazyTickRate(Math.max(1, delay.getValue()));
        }
    }

    // --- Capability cache yardımcıları (chute'tan alınan pattern) ---

    private IItemHandler grabCapability(Direction side) {
        BlockPos checkPos = worldPosition.relative(side);
        Direction opposite = side.getOpposite();

        IItemHandler cached;
        if (level instanceof ServerLevel serverLevel) {
            cached = capCaches.computeIfAbsent(side, s ->
                    BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, serverLevel, checkPos, opposite)
            ).getCapability();
        } else {
            cached = level.getCapability(Capabilities.ItemHandler.BLOCK, checkPos, opposite);
        }

        // Normal lookup başarısızsa ve Sable yüklüyse, sub-level üzerinden dene
        return SableCompatDispatcher.grabCapabilityWithFallback(level, cached, checkPos, opposite);
    }

    @Override
    public void invalidate() {
        capCaches.clear();
        super.invalidate();
    }

    // --- Tick mantığı ---

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        ItemStack current = inventory.getStackInSlot(0);
        if (!current.isEmpty()) {
            tryOutputItem();
        }

        IItemHandler handlerAbove = grabCapability(inputDir());
        if (handlerAbove != null) return;

        scanAndCollect();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide) return;
        if (!inventory.getStackInSlot(0).isEmpty()) return;

        IItemHandler handlerAbove = grabCapability(inputDir());
        if (handlerAbove == null) return;
        if (invTracker.stillWaiting(handlerAbove)) return;

        for (int i = 0; i < handlerAbove.getSlots(); i++) {
            ItemStack stackInSlot = handlerAbove.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            ItemStack extracted = handlerAbove.extractItem(i, stackInSlot.getCount(), false);
            if (!extracted.isEmpty()) {
                inventory.setStackInSlot(0, extracted);
                invTracker.awaitNewVersion(handlerAbove);
                break;
            }
        }

        tryOutputItem(); // envanterde item varsa hemen çıkışı da dene
    }

    private boolean canAcceptItem(ItemStack incoming) {
        ItemStack current = inventory.getStackInSlot(0);
        if (current.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(current, incoming)
                && current.getCount() + incoming.getCount() <= current.getMaxStackSize();
    }

    private void scanAndCollect() {
        BlockPos target = worldPosition.relative(inputDir());

        AABB scanArea = new AABB(
                target.getX(),
                target.getY(),
                target.getZ(),
                target.getX() + 1,
                target.getY() + 1,
                target.getZ() + 1
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanArea);
        for (ItemEntity itemEntity : items) {
            ItemStack entityStack = itemEntity.getItem();
            if (itemEntity.getAge() >= delay.getValue() && canAcceptItem(entityStack)) {
                ItemStack current = inventory.getStackInSlot(0);
                if (current.isEmpty()) {
                    inventory.setStackInSlot(0, entityStack.copy());
                } else {
                    current.grow(entityStack.getCount());
                }
                itemEntity.discard();
                tryOutputItem();
                break;
            }
        }
    }

    // Sadece envantere koyar / komşu envantere aktarır - YERE ITEM ATMAZ
    private void tryOutputItem() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty()) return;

        IItemHandler handlerBelow = grabCapability(outputDir());
        if (handlerBelow != null) {
            inventory.setStackInSlot(0, ItemHandlerHelper.insertItem(handlerBelow, stack, false));
        }
        // handlerBelow null ise hiçbir şey yapma, item kendi envanterinde beklemeye devam eder
    }
}