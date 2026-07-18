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
import net.xenrao.create_random_bulksheet.RandomBulkSheetConfig;
import net.xenrao.create_random_bulksheet.blocks.RandomBulkSheetBlockEntities;

import java.util.List;


public class AbyssalEnergyTankBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    protected int starCount;
    protected int netheriteCount;
    protected int diamondCount;

    private AbyssalEnergyStorage energy;
    protected IEnergyStorage energyCapability;

    public AbyssalEnergyTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        refreshCapability();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                RandomBulkSheetBlockEntities.ABYSSAL_ENERGY_TANK.get(),
                (be, side) -> {
                    if (be.energyCapability == null)
                        be.refreshCapability();
                    return be.energyCapability;
                }
        );
    }

    private long computeCapacity() {
        int baseCapacity = RandomBulkSheetConfig.ABYSSAL_ENERGY_TANK_BASE_CAPACITY.get();
        int starCapacityBonus = RandomBulkSheetConfig.ABYSSAL_ENERGY_TANK_STAR_CAPACITY_BONUS.get();
        int netheriteCapacityBonus = RandomBulkSheetConfig.ABYSSAL_ENERGY_TANK_NETHERITE_CAPACITY_BONUS.get();
        int diamondCapacityBonus = RandomBulkSheetConfig.ABYSSAL_ENERGY_TANK_DIAMOND_CAPACITY_BONUS.get();

        return (long) baseCapacity
                + (long) starCount * starCapacityBonus
                + (long) netheriteCount * netheriteCapacityBonus
                + (long) diamondCount * diamondCapacityBonus;
    }

    private void refreshCapability() {
        int oldEnergy = energy == null ? 0 : energy.getEnergyStored();
        int finalCapacity = (int) Math.min(computeCapacity(), Integer.MAX_VALUE);

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
        energyCapability = energy;
        invalidateCapabilities();
    }

    public ItemInteractionResult addGems(Player player, ItemStack item) {
        if (energy.getMaxEnergyStored() == Integer.MAX_VALUE)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (item.is(Items.NETHER_STAR)) {
            starCount++;
        } else if (item.is(Items.NETHERITE_INGOT)) {
            netheriteCount++;
        } else if (item.is(Items.DIAMOND)) {
            diamondCount++;
        }

        if (!player.isCreative())
            item.shrink(1);
        refreshCapability();
        setChanged();
        sendData();
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        starCount = tag.getInt("StarCount");
        netheriteCount = tag.getInt("NetheriteCount");
        diamondCount = tag.getInt("DiamondCount");
        refreshCapability();
        if (energy != null)
            energy.setEnergy(tag.getInt("Energy"));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("StarCount", starCount);
        tag.putInt("NetheriteCount", netheriteCount);
        tag.putInt("DiamondCount", diamondCount);
        tag.putInt("Energy", energy.getEnergyStored());
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
                    .add(CreateLang.number(energy.getMaxEnergyStored()).style(ChatFormatting.GOLD))
                    .add(CreateLang.builder().text(" FE").style(ChatFormatting.GOLD))
                    .forGoggles(tooltip, 1);
        } else {
            CreateLang.builder()
                    .text("Forge Energy")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
            CreateLang.builder()
                    .add(CreateLang.number(energy.getEnergyStored()).style(ChatFormatting.GOLD))
                    .add(CreateLang.builder().text(" FE").style(ChatFormatting.GOLD))
                    .add(CreateLang.builder().text(" / ").style(ChatFormatting.GRAY))
                    .add(CreateLang.number(energy.getMaxEnergyStored()).style(ChatFormatting.DARK_GRAY))
                    .add(CreateLang.builder().text(" FE").style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
        }

        if ((starCount + netheriteCount + diamondCount > 0 || isPlayerSneaking)) {
            tooltip.add(Component.empty());
            CreateLang.builder()
                    .text("Upgrades:")
                    .style(ChatFormatting.LIGHT_PURPLE)
                    .forGoggles(tooltip);

            int starCapacityBonus = RandomBulkSheetConfig.ABYSSAL_ENERGY_TANK_STAR_CAPACITY_BONUS.get();
            int netheriteCapacityBonus = RandomBulkSheetConfig.ABYSSAL_ENERGY_TANK_NETHERITE_CAPACITY_BONUS.get();
            int diamondCapacityBonus = RandomBulkSheetConfig.ABYSSAL_ENERGY_TANK_DIAMOND_CAPACITY_BONUS.get();

            if (starCount > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Nether Star: ")
                        .style(ChatFormatting.YELLOW)
                        .add(CreateLang.builder().text("x" + starCount).style(ChatFormatting.GREEN));
                if (isPlayerSneaking) {
                    builder.add(CreateLang.builder().text(" (").style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) starCount * starCapacityBonus).style(ChatFormatting.BLUE))
                            .add(CreateLang.builder().text(" FE").style(ChatFormatting.BLUE))
                            .add(CreateLang.builder().text(")").style(ChatFormatting.GRAY));
                }
                builder.forGoggles(tooltip, 1);
            }
            if (netheriteCount > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Netherite Ingot: ")
                        .style(ChatFormatting.RED)
                        .add(CreateLang.builder().text("x" + netheriteCount).style(ChatFormatting.GREEN));
                if (isPlayerSneaking) {
                    builder.add(CreateLang.builder().text(" (").style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) netheriteCount * netheriteCapacityBonus).style(ChatFormatting.BLUE))
                            .add(CreateLang.builder().text(" FE").style(ChatFormatting.BLUE))
                            .add(CreateLang.builder().text(")").style(ChatFormatting.GRAY));
                }
                builder.forGoggles(tooltip, 1);
            }
            if (diamondCount > 0 || isPlayerSneaking) {
                LangBuilder builder = CreateLang.builder()
                        .text("Diamond: ")
                        .style(ChatFormatting.AQUA)
                        .add(CreateLang.builder().text("x" + diamondCount).style(ChatFormatting.GREEN));
                if (isPlayerSneaking) {
                    builder.add(CreateLang.builder().text(" (").style(ChatFormatting.GRAY))
                            .add(CreateLang.number((long) diamondCount * diamondCapacityBonus).style(ChatFormatting.BLUE))
                            .add(CreateLang.builder().text(" FE").style(ChatFormatting.BLUE))
                            .add(CreateLang.builder().text(")").style(ChatFormatting.GRAY));
                }
                builder.forGoggles(tooltip, 1);
            }
        }

        return true;
    }
}