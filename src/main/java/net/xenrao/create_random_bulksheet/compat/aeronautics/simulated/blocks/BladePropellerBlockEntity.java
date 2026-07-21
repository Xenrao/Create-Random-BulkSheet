package net.xenrao.create_random_bulksheet.compat.aeronautics.simulated.blocks;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xenrao.create_random_bulksheet.RandomBulkSheetConfig;
import net.xenrao.create_random_bulksheet.compat.aeronautics.RandomBulkSheetAeronauticsItems;

import java.util.List;

public class BladePropellerBlockEntity extends BasePropellerBlockEntity {

    private static final double BASE_THRUST = 1.0;
    private static final double BASE_AIRFLOW = 0.1;

    private static final double BASELINE_ANGLE = 15.0;

    private int smallBladeCount = 0;
    private int largeBladeCount = 0;

    private ScrollValueBehaviour angleController;

    private double efficiency = 0;
    private double oldEfficiency = 0;

    protected final ItemStackHandler inventory = new ItemStackHandler(8) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    private VersionedInventoryTrackerBehaviour invTracker;

    protected int bladeAngle = 0;

    public BladePropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        invTracker = new VersionedInventoryTrackerBehaviour(this);
        behaviours.add(invTracker);

        super.addBehaviours(behaviours);
        angleController = new ScrollValueBehaviour(
                Component.translatable("create_random_bulksheet.blade_propeller.angle"),
                this,
                new ValueBoxTransform.Sided() {
                    @Override
                    protected Vec3 getSouthLocation() {
                        return new Vec3(0.5, 0.5, 0.87);
                    }

                    @Override
                    protected boolean isSideActive(BlockState state, Direction direction) {
                        return direction == state.getValue(BlockStateProperties.FACING);
                    }
                }

        ) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                ImmutableList<Component> rows = ImmutableList.of(Component.literal("⟳")
                                .withStyle(ChatFormatting.BOLD),
                        Component.literal("⟲")
                                .withStyle(ChatFormatting.BOLD));
                ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
                return new ValueSettingsBoard(label, getMaxBladeAngle(), 10, rows, formatter);
            }

            @Override
            public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
                int val = settings.value();
                if (settings.row() == 1) {
                    bladeAngle = val;
                    BlockState state = getBlockState().setValue(BladePropellerBlock.REVERSED, true);
                    level.setBlock(worldPosition, state, 3);

                } else {
                    bladeAngle = val * -1;
                    BlockState state = getBlockState().setValue(BladePropellerBlock.REVERSED, false);
                    level.setBlock(worldPosition, state, 3);
                }
                setValue(val);
                playFeedbackSound(this);
            }

            @Override
            public ValueSettings getValueSettings() {
                return new ValueSettings(getBlockState().getValue(BladePropellerBlock.REVERSED) ? 1 : 0, value);
            }

            public MutableComponent formatSettings(ValueSettings settings) {
                return Component.literal("" + settings.value());
            }

        };

        angleController.between(0, getMaxBladeAngle());
        angleController.value = 0;
        angleController.withCallback(this::onAngleChanged);
        behaviours.add(angleController);
    }

    private int getMaxBladeAngle() {
        return RandomBulkSheetConfig.BLADE_PROPELLER_MAX_BLADE_ANGLE.get();
    }

    private void onAngleChanged(int newAngle) {
        recalculateEfficiency();
        notifyUpdate();
    }

    public int getBladeCount() {
        return (this.smallBladeCount + this.largeBladeCount);
    }

    public void addBlade(ItemStack blade, Player player) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {

                inventory.insertItem(i, blade, false);
                if (blade.is(RandomBulkSheetAeronauticsItems.SMALL_PROPELLER_BLADE)) {
                    this.smallBladeCount++;
                    if (!player.isCreative())
                        blade.shrink(1);
                } else
                    this.largeBladeCount++;
                break;
            }

        }
        recalculateEfficiency();
        notifyUpdate();
    }

    public void removeBlade(Player player) {
        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            ItemStack removed = inventory.extractItem(i, 1, false);

            if (removed.is(RandomBulkSheetAeronauticsItems.SMALL_PROPELLER_BLADE)) {
                this.smallBladeCount--;
            } else {
                this.largeBladeCount--;
            }

            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }
            recalculateEfficiency();
            notifyUpdate();
            return;
        }
    }

    @Override
    protected float getDirectionIndependentSpeed() {
        return this.getBlockDirection().getAxisDirection().getStep() * this.rotationSpeed * (10f / 3) * (this.getBlockState().getValue(BasePropellerBlock.REVERSED) ? -1 : 1);
    }

    private void recalculateEfficiency() {
        double bladeUnits = smallBladeCount * 1.0 + largeBladeCount * 2.0;
        double angle = angleController != null ? angleController.getValue() : 0;
        this.efficiency = (bladeUnits) * (angle / BASELINE_ANGLE);
        getOrCreateNetwork();

        if (this.oldEfficiency != this.efficiency) {
            this.oldEfficiency = this.efficiency;
            if (hasNetwork())
                getOrCreateNetwork().remove(this);
            RotationPropagator.handleRemoved(level, worldPosition, this);
            removeSource();
            attachKinetics();

        }
    }

    @Override
    public double getConfigThrust() {
        return BASE_THRUST * efficiency;
    }

    @Override
    public double getConfigAirflow() {
        return BASE_AIRFLOW * efficiency;
    }

    @Override
    public float getRadius() {
        return 1;
    }

    @Override
    public float getOffset() {
        return 3 / 16f;
    }

    @Override
    public float calculateStressApplied() {
        float impact = RandomBulkSheetConfig.BLADE_PROPELLER_STRESS_IMPACT.get().floatValue() * (float) efficiency;
        this.lastStressApplied = impact;
        return impact;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("SmallBlades", smallBladeCount);
        tag.putInt("LargeBlades", largeBladeCount);
        tag.putInt("BladeAngle", bladeAngle);
        tag.putDouble("Efficiency", efficiency);
        tag.putDouble("OldEfficiency", oldEfficiency);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        smallBladeCount = tag.getInt("SmallBlades");
        largeBladeCount = tag.getInt("LargeBlades");
        bladeAngle = tag.getInt("BladeAngle");
        efficiency = tag.getDouble("Efficiency");
        oldEfficiency = tag.getDouble("OldEfficiency");
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        recalculateEfficiency();
    }
}