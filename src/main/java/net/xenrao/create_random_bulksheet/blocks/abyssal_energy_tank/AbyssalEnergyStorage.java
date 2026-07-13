package net.xenrao.create_random_bulksheet.blocks.abyssal_energy_tank;

import net.neoforged.neoforge.energy.EnergyStorage;
public class AbyssalEnergyStorage extends EnergyStorage {

    private final Runnable onChange;

    public AbyssalEnergyStorage(
            int capacity,
            int maxTransfer,
            int energy,
            Runnable onChange
    ) {
        super(capacity, maxTransfer, maxTransfer, energy);
        this.onChange = onChange;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, capacity);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int result = super.receiveEnergy(maxReceive, simulate);

        if (result > 0 && !simulate)
            onChange.run();

        return result;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int result = super.extractEnergy(maxExtract, simulate);

        if (result > 0 && !simulate)
            onChange.run();

        return result;
    }
}