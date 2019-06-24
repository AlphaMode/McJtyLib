package mcjty.lib.tileentity;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.EnergyTools;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.energy.IEnergyStorage;

public class GenericEnergyStorage implements IEnergyStorage {

    private final GenericTileEntity tileEntity;
    private final boolean isReceiver;

    private long energy;
    private long capacity;
    private long maxReceive;

    public GenericEnergyStorage(GenericTileEntity tileEntity, boolean isReceiver, long capacity, long maxReceive) {
        this.tileEntity = tileEntity;
        this.isReceiver = isReceiver;

        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.energy = 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (isReceiver) {
            if (!canReceive()) {
                return 0;
            }

            long energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
            if (!simulate) {
                energy += energyReceived;
                tileEntity.markDirtyQuick();
            }
            return (int) energyReceived;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return EnergyTools.getIntEnergyStored(energy, capacity);
    }

    public long getEnergy() {
        return energy;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setEnergy(long s) {
        energy = s;
    }

    public void consumeEnergy(long energy) {
        if (energy > capacity - this.energy) {
            energy = capacity - this.energy;
        } else if (energy < -this.energy) {
            energy = -this.energy;
        }
        this.energy += energy;
        tileEntity.markDirtyQuick();
    }

    @Override
    public int getMaxEnergyStored() {
        return EnergyTools.unsignedClampToInt(capacity);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return isReceiver;
    }

    public void addIntegerListeners(GenericContainer container) {
        // Least significant part
        container.addIntegerListener(new IntReferenceHolder() {
            @Override
            public int get() {
                return (int) (getEnergy());     // Least significant bits
            }

            @Override
            public void set(int i) {
                long orig = getEnergy() & ~0xffffffffL;
                orig |= i;
                setEnergy(orig);
            }
        });
        // Most significant part
        container.addIntegerListener(new IntReferenceHolder() {
            @Override
            public int get() {
                return (int) (getEnergy() >> 32L);     // Most significant bits
            }

            @Override
            public void set(int i) {
                long orig = getEnergy() & 0xffffffffL;
                orig |= (long) i << 32L;
                setEnergy(orig);
            }
        });
    }
}