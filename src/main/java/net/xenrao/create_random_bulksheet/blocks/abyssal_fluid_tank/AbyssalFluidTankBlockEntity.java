package net.xenrao.create_random_bulksheet.blocks.abyssal_fluid_tank;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.xenrao.create_random_bulksheet.RandomBulkSheetConfig;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;

import java.util.List;

public class AbyssalFluidTankBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private SmartFluidTank tank;
    protected IFluidHandler fluidCapability;

    protected int starCount;
    protected int netheriteCount;
    protected int diamondCount;
    protected boolean infinite;

    public AbyssalFluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tank = createInventory();
        refreshCapability();
    }

    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(1000, this::onFluidChanged) {

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                if (infinite && getFluidAmount() >= getInfiniteThreshold()) {
                    FluidStack result = getFluid().copy();
                    result.setAmount(Math.min(maxDrain, result.getAmount()));
                    return result;
                }
                return super.drain(maxDrain, action);
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (infinite && getFluidAmount() >= getInfiniteThreshold()) {
                    FluidStack result = getFluid().copy();
                    result.setAmount(Math.min(resource.getAmount(), result.getAmount()));
                    return result;
                }
                return super.drain(resource, action);
            }
        };
    }

    private void onFluidChanged(FluidStack fluidStack) {
        if (!hasLevel()) return;
        setChanged();
        if (!level.isClientSide) sendData();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, RandomBulkSheetBlockEntities.ABYSSAL_FLUID_TANK.get(), (be, context) -> {
            if (be.fluidCapability == null) be.refreshCapability();
            return be.fluidCapability;
        });
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    private static int getInfiniteThreshold() {
        return AllConfigs.server().fluids.hosePulleyBlockThreshold.get();
    }

    private long computeCapacity() {
        int baseCapacity = RandomBulkSheetConfig.ABYSSAL_FLUID_TANK_BASE_CAPACITY.get();
        int starCapacityBonus = RandomBulkSheetConfig.ABYSSAL_FLUID_TANK_STAR_CAPACITY_BONUS.get();
        int netheriteCapacityBonus = RandomBulkSheetConfig.ABYSSAL_FLUID_TANK_NETHERITE_CAPACITY_BONUS.get();
        int diamondCapacityBonus = RandomBulkSheetConfig.ABYSSAL_FLUID_TANK_DIAMOND_CAPACITY_BONUS.get();

        return ((long) baseCapacity
                + (long) starCount * starCapacityBonus
                + (long) netheriteCount * netheriteCapacityBonus
                + (long) diamondCount * diamondCapacityBonus);
    }

    void refreshCapability() {
        long capacity = computeCapacity();
        tank.setCapacity((int) Math.min(capacity, Integer.MAX_VALUE));
        fluidCapability = tank;
        invalidateCapabilities();
    }

    public ItemInteractionResult addGems(Player player, ItemStack item) {
        if (item.is(RandomBulkSheetItems.VOID_STAR.get())) {
            if (infinite)
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            else
                infinite = true;
        } else if (tank.getCapacity() == Integer.MAX_VALUE)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (item.is(Items.NETHER_STAR)) {
            starCount++;
        } else if (item.is(Items.NETHERITE_INGOT)) {
            netheriteCount++;
        } else if (item.is(Items.DIAMOND)) {
            diamondCount++;
        }

        if (!player.isCreative()) item.shrink(1);

        refreshCapability();
        setChanged();

        if (!level.isClientSide)
            sendData();

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        starCount = tag.getInt("StarCount");
        netheriteCount = tag.getInt("NetheriteCount");
        diamondCount = tag.getInt("DiamondCount");
        infinite = tag.getBoolean("Infinite");

        refreshCapability();

        tank.readFromNBT(registries, tag.getCompound("Tank"));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("StarCount", starCount);
        tag.putInt("NetheriteCount", netheriteCount);
        tag.putInt("DiamondCount", diamondCount);
        tag.putBoolean("Infinite", infinite);

        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));

        super.write(tag, registries, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        containedFluidTooltip(tooltip, isPlayerSneaking, fluidCapability);

        if ((starCount + netheriteCount + diamondCount > 0 || isPlayerSneaking)) {

            tooltip.add(Component.empty());

            CreateLang.builder()
                    .text("Upgrades:")
                    .style(ChatFormatting.LIGHT_PURPLE)
                    .forGoggles(tooltip);

            int starCapacityBonus = RandomBulkSheetConfig.ABYSSAL_FLUID_TANK_STAR_CAPACITY_BONUS.get();
            int netheriteCapacityBonus = RandomBulkSheetConfig.ABYSSAL_FLUID_TANK_NETHERITE_CAPACITY_BONUS.get();
            int diamondCapacityBonus = RandomBulkSheetConfig.ABYSSAL_FLUID_TANK_DIAMOND_CAPACITY_BONUS.get();

            if (starCount > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Nether Star: ")
                        .style(ChatFormatting.YELLOW)
                        .add(CreateLang.builder()
                                .text("x" + starCount)
                                .style(ChatFormatting.GREEN));

                if (isPlayerSneaking) {
                    builder
                            .add(CreateLang.builder()
                                    .text(" (")
                                    .style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) starCount * starCapacityBonus)
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text("mB")
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text(")")
                                    .style(ChatFormatting.GRAY));
                }

                builder.forGoggles(tooltip, 1);
            }

            if (netheriteCount > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Netherite Ingot: ")
                        .style(ChatFormatting.RED)
                        .add(CreateLang.builder()
                                .text("x" + netheriteCount)
                                .style(ChatFormatting.GREEN));

                if (isPlayerSneaking) {
                    builder
                            .add(CreateLang.builder()
                                    .text(" (")
                                    .style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) netheriteCount * netheriteCapacityBonus)
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text("mB")
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text(")")
                                    .style(ChatFormatting.GRAY));
                }

                builder.forGoggles(tooltip, 1);
            }

            if (diamondCount > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Diamond: ")
                        .style(ChatFormatting.AQUA)
                        .add(CreateLang.builder()
                                .text("x" + diamondCount)
                                .style(ChatFormatting.GREEN));

                if (isPlayerSneaking) {
                    builder
                            .add(CreateLang.builder()
                                    .text(" (")
                                    .style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) diamondCount * diamondCapacityBonus)
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text("mB")
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text(")")
                                    .style(ChatFormatting.GRAY));
                }

                builder.forGoggles(tooltip, 1);
            }
        }

        boolean thresholdReached = tank.getFluidAmount() >= getInfiniteThreshold();

        if (thresholdReached || infinite || isPlayerSneaking) {
            tooltip.add(Component.empty());

            CreateLang.builder()
                    .text("Bottomless Supply")
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip);

            if (infinite && thresholdReached) {
                CreateLang.builder()
                        .text("Fluid inside is now considered infinite.")
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip, 1);

            } else {

                CreateLang.builder()
                        .text("This block has the potential to become an infinite source.")
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip, 1);

                if (!infinite) {
                    CreateLang.builder()
                            .text("Requires a ")
                            .style(ChatFormatting.GRAY)
                            .add(CreateLang.builder()
                                    .text("Void Star")
                                    .style(ChatFormatting.LIGHT_PURPLE))
                            .text(".")
                            .style(ChatFormatting.GRAY)
                            .forGoggles(tooltip, 1);
                }

                if (!thresholdReached) {

                    long missing = getInfiniteThreshold() - tank.getFluidAmount();

                    CreateLang.builder()
                            .text("Needs ")
                            .style(ChatFormatting.GRAY)
                            .add(CreateLang.number(missing)
                                    .style(ChatFormatting.GOLD))
                            .add(CreateLang.builder()
                                    .text("mB")
                                    .style(ChatFormatting.GOLD))
                            .text(" more fluid.")
                            .style(ChatFormatting.GRAY)
                            .forGoggles(tooltip, 1);
                }
            }
        }
        return true;
    }
}