package net.xenrao.create_random_bulksheet.blocks.delayed_transporter;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public class DelayedTransporterBlockEntity extends SmartBlockEntity {

    private final ItemStackHandler inventory = new ItemStackHandler(1);
    private VersionedInventoryTrackerBehaviour invTracker;
    private ScrollValueBehaviour ageThreshold;

    public DelayedTransporterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        invTracker = new VersionedInventoryTrackerBehaviour(this);
        behaviours.add(invTracker);

        ageThreshold = new ScrollValueBehaviour(
                Component.translatable("create_random_bulksheet.delayed_transporter.age_threshold"),
                this,
                new com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform.Sided() {
                    @Override
                    protected Vec3 getSouthLocation() { return new Vec3(0.5, 0.5, 1.0); }
                    @Override
                    protected boolean isSideActive(BlockState state, Direction direction) { return direction.getAxis().isHorizontal(); }
                }
        ) {
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, 99, 10,
                        com.simibubi.create.foundation.utility.CreateLang.translatedOptions("generic.unit", "ticks", "seconds"),
                        new ValueSettingsFormatter(this::formatSettings));
            }

            @Override
            public ValueSettings getValueSettings() {
                int val = Math.max(5, this.value);
                int row = (val >= 200) ? 1 : 0;
                int displayVal = (row == 1) ? (val / 20) : val;
                return new ValueSettings(row, Math.max(5, displayVal));
            }

            @Override
            public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
                int val = settings.value();
                if (settings.row() == 1) {
                    val = Math.max(10, val);
                    setValue(Math.min(2000, val * 20));
                } else {
                    val = Math.max(5, val);
                    setValue(Math.min(2000, val));
                }
                playFeedbackSound(this);
            }

            public MutableComponent formatSettings(ValueSettings settings) {
                int v = settings.value();
                return Component.literal(settings.row() == 1 ? Math.max(10, v) + "s" : Math.max(5, v) + "t");
            }
        };

        ageThreshold.between(5, 2000);
        ageThreshold.setValue(10);
        ageThreshold.withCallback(this::onThresholdChanged);
        behaviours.add(ageThreshold);
    }

    private void onThresholdChanged(int newMax) {
        setLazyTickRate(newMax);
        this.sendData();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("LazyTickRate", this.lazyTickRate);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("LazyTickRate")) this.lazyTickRate = tag.getInt("LazyTickRate");
    }

    @Override
    public void tick(){
        super.tick();
        if (!inventory.getStackInSlot(0).isEmpty()) {
            tryOutputItem();
        }

    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        if (inventory.getStackInSlot(0).isEmpty()) {
            BlockPos posAbove = worldPosition.above();
            IItemHandler handlerAbove = level.getCapability(Capabilities.ItemHandler.BLOCK, posAbove, Direction.DOWN);

            if (handlerAbove != null) {
                if (!invTracker.stillWaiting(handlerAbove)) {
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
                }
            } else {
                scanAndCollect();
            }
        }
    }

    private void scanAndCollect() {
        AABB scanArea = new AABB(
                worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 1.5, worldPosition.getZ() + 1
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanArea);
        for (ItemEntity itemEntity : items) {
            if (itemEntity.getAge() >= ageThreshold.getValue()) {
                inventory.setStackInSlot(0, itemEntity.getItem().copy());
                itemEntity.discard();
                break;
            }
        }
    }

    private void tryOutputItem() {
        ItemStack stack = inventory.getStackInSlot(0);
        BlockPos posBelow = worldPosition.below();
        IItemHandler handlerBelow = level.getCapability(Capabilities.ItemHandler.BLOCK, posBelow, Direction.UP);

        if (handlerBelow != null) {
            inventory.setStackInSlot(0, ItemHandlerHelper.insertItem(handlerBelow, stack, false));
        } else {
            ItemEntity itemEntity = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() - 0.35, worldPosition.getZ() + 0.5, stack);
            itemEntity.setDeltaMovement(0, -0.05, 0);
            level.addFreshEntity(itemEntity);
            inventory.setStackInSlot(0, ItemStack.EMPTY);
        }
    }
}