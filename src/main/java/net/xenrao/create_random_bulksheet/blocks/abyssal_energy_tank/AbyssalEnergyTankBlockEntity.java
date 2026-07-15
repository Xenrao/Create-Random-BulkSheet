package net.xenrao.create_random_bulksheet.blocks.abyssal_energy_tank;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.xenrao.create_random_bulksheet.Config;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;

import java.util.List;


public class AbyssalEnergyTankBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private final int base_capacity = Config.ENERGY_TANK_BASE_CAPACITY.get();// 1M FE

    protected int star_count;
    protected int netherite_count;
    protected int diamond_count;

    private final int star_cap = Config.ENERGY_TANK_STAR_CAP.get();
    private final int netherite_cap = Config.ENERGY_TANK_NETHERITE_CAP.get();
    private final int diamond_cap = Config.ENERGY_TANK_DIAMOND_CAP.get();

    private AbyssalEnergyStorage energy;

    protected IEnergyStorage energy_capability;


    public AbyssalEnergyTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        refreshCapability();
    }


    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                RandomBulkSheetBlockEntities.ABYSSAL_ENERGY_TANK.get(),
                (be, side) -> {
                    if (be.energy_capability == null)
                        be.refreshCapability();

                    return be.energy_capability;
                }
        );
    }


    private void refreshCapability() {
        int oldEnergy = energy == null ? 0 : energy.getEnergyStored();

        long capacity =
                (long) base_capacity
                        + (long) star_count * star_cap
                        + (long) netherite_count * netherite_cap
                        + (long) diamond_count * diamond_cap;

        int finalCapacity = (int) Math.min(capacity, Integer.MAX_VALUE);

        energy = new AbyssalEnergyStorage(
                finalCapacity,
                Integer.MAX_VALUE,
                Math.min(oldEnergy, finalCapacity),
                () -> {
                    setChanged();

                    if (level != null && !level.isClientSide)
                        sendData();
                }
        );

        energy_capability = energy;
        invalidateCapabilities();
    }


    public ItemInteractionResult addGems(Player player, ItemStack item) {
        if (energy.getMaxEnergyStored() == Integer.MAX_VALUE)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (item.is(Items.NETHER_STAR)) {
            star_count++;

        } else if (item.is(Items.NETHERITE_INGOT)) {
            netherite_count++;

        } else if (item.is(Items.DIAMOND)) {
            diamond_count++;

        }

        if (!player.isCreative())
            item.shrink(1);

        refreshCapability();
        setChanged();

        return ItemInteractionResult.SUCCESS;
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

/*
    @Override
    public void lazyTick() {
        super.lazyTick();
        if (star_count + netherite_count + diamond_count > 0 && base_capacity == 1000000)
            refreshCapability();
    }
*/

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        star_count = tag.getInt("StarCount");
        netherite_count = tag.getInt("NetheriteCount");
        diamond_count = tag.getInt("DiamondCount");
        refreshCapability();
        if (energy != null)
            energy.setEnergy(tag.getInt("Energy"));

    }


    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putInt("StarCount", star_count);
        tag.putInt("NetheriteCount", netherite_count);
        tag.putInt("DiamondCount", diamond_count);

        tag.putInt(
                "Energy",
                energy.getEnergyStored()
        );

    }


    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

        CreateLang.builder()
                .text("Energy Container Info: ")
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);

        if (energy.getEnergyStored() == 0) {
            CreateLang.builder()
                    .text("Capacity: ")
                    .style(ChatFormatting.GRAY)

                    .add(CreateLang.number(
                            energy.getMaxEnergyStored()
                    ).style(ChatFormatting.GOLD))

                    .add(CreateLang.builder()
                            .text(" FE")
                            .style(ChatFormatting.GOLD))

                    .forGoggles(tooltip, 1);
        } else {
            CreateLang.builder()
                    .text("Forge Energy")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);

            CreateLang.builder()
                    .add(CreateLang.number(
                            energy.getEnergyStored()
                    ).style(ChatFormatting.GOLD))

                    .add(CreateLang.builder()
                            .text(" FE")
                            .style(ChatFormatting.GOLD))

                    .add(CreateLang.builder()
                            .text(" / ")
                            .style(ChatFormatting.GRAY))

                    .add(CreateLang.number(
                            energy.getMaxEnergyStored()
                    ).style(ChatFormatting.DARK_GRAY))

                    .add(CreateLang.builder()
                            .text(" FE")
                            .style(ChatFormatting.DARK_GRAY))

                    .forGoggles(tooltip, 1);

        }

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
                            .add(CreateLang.number((long) star_count * star_cap)
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text(" FE")
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
                            .add(CreateLang.number((long) netherite_count * netherite_cap)
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text(" FE")
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
                            .add(CreateLang.number((long) diamond_count * diamond_cap)
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text(" FE")
                                    .style(ChatFormatting.BLUE))
                            .add(CreateLang.builder()
                                    .text(")")
                                    .style(ChatFormatting.GRAY));
                }

                builder.forGoggles(tooltip, 1);
            }

        }

        return true;
    }
}