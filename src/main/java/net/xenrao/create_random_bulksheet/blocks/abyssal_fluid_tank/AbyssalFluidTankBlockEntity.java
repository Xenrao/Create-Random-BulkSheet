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
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;
import net.xenrao.create_random_bulksheet.items.RandomBulkSheetItems;

import java.util.List;


public class AbyssalFluidTankBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private SmartFluidTank tank;

    protected IFluidHandler fluid_capability;

    private int tank_base_capacity = 1000;
    private int tank_capacity;

    protected int star_count;
    protected int netherite_count;
    protected int diamond_count;

    private int star_mb = 200;
    private int netherite_mb = 50;
    private int diamond_mb = 10;
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
            if (be.fluid_capability == null) be.refreshCapability();
            return be.fluid_capability;
        });
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }


    private static int getInfiniteThreshold() {
        return AllConfigs.server().fluids.hosePulleyBlockThreshold.get() * 1000;
    }

    void refreshCapability() {
        tank_capacity = (tank_base_capacity + (star_count * star_mb) + (netherite_count * netherite_mb) + (diamond_count * diamond_mb)) * 1000;
        if (0 > tank_capacity)
            tank_capacity = Integer.MAX_VALUE;
        tank.setCapacity(tank_capacity);
        fluid_capability = tank;
        invalidateCapabilities();
    }


    public ItemInteractionResult addGems(Player player, ItemStack item) {
        if (item.is(RandomBulkSheetItems.VOID_STAR.get())) {
            if (infinite)
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            else
                infinite = true;

        } else if (tank_capacity == Integer.MAX_VALUE)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (item.is(Items.NETHER_STAR)) {
            star_count++;

        } else if (item.is(Items.NETHERITE_INGOT)) {
            netherite_count++;

        } else if (item.is(Items.DIAMOND)) {
            diamond_count++;

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

        star_count = tag.getInt("StarCount");
        netherite_count = tag.getInt("NetheriteCount");
        diamond_count = tag.getInt("DiamondCount");
        infinite = tag.getBoolean("Infinite");

        refreshCapability();

        tank.readFromNBT(registries, tag.getCompound("Tank"));
    }


    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {

        tag.putInt("StarCount", star_count);
        tag.putInt("NetheriteCount", netherite_count);
        tag.putInt("DiamondCount", diamond_count);
        tag.putBoolean("Infinite", infinite);

        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));

        super.write(tag, registries, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        containedFluidTooltip(
                tooltip,
                isPlayerSneaking,
                fluid_capability
        );


        if ((star_count + netherite_count + diamond_count > 0 || isPlayerSneaking)) {

            tooltip.add(Component.empty());

            CreateLang.builder()
                    .text("Upgrades:")
                    .style(ChatFormatting.LIGHT_PURPLE)
                    .forGoggles(tooltip);

            if (star_count > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Nether Star: ")
                        .style(ChatFormatting.YELLOW)
                        .add(CreateLang.builder()
                                .text("x" + star_count)
                                .style(ChatFormatting.GREEN));

                if (isPlayerSneaking) {
                    builder
                            .add(CreateLang.builder()
                                    .text(" (")
                                    .style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) star_count * star_mb * 1000)
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


            if (netherite_count > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Netherite Ingot: ")
                        .style(ChatFormatting.RED)
                        .add(CreateLang.builder()
                                .text("x" + netherite_count)
                                .style(ChatFormatting.GREEN));

                if (isPlayerSneaking) {
                    builder
                            .add(CreateLang.builder()
                                    .text(" (")
                                    .style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) netherite_count * netherite_mb * 1000)
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


            if (diamond_count > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Diamond: ")
                        .style(ChatFormatting.AQUA)
                        .add(CreateLang.builder()
                                .text("x" + diamond_count)
                                .style(ChatFormatting.GREEN));

                if (isPlayerSneaking) {
                    builder
                            .add(CreateLang.builder()
                                    .text(" (")
                                    .style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) diamond_count * diamond_mb * 1000)
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

        boolean thresholdReached =
                tank.getFluidAmount() >= getInfiniteThreshold();

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

                    long missing =
                            getInfiniteThreshold() - tank.getFluidAmount();

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