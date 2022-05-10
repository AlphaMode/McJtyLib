package mcjty.lib.varia;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import mcjty.lib.api.power.IBigPower;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import team.reborn.energy.api.EnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemCapabilityProvider implements ICapabilityProvider, IBigPower {

    private final ItemStack itemStack;
    private final IEnergyItem item;

    private final LazyOptional<EnergyStorage> energy = LazyOptional.of(this::createEnergyStorage);

    @Nonnull
    private <T> EnergyStorage createEnergyStorage() {
        return new EnergyStorage() {
            @Override
            public long insert(long maxReceive, TransactionContext tx) {
                return item.receiveEnergyL(itemStack, maxReceive, tx);
            }

            @Override
            public long extract(long maxExtract, TransactionContext tx) {
                return item.extractEnergyL(itemStack, maxExtract, tx);
            }

            @Override
            public long getAmount() {
                return item.getEnergyStoredL(itemStack);
            }

            @Override
            public long getCapacity() {
                return EnergyTools.unsignedClampToInt(item.getMaxEnergyStoredL(itemStack));
            }
        };
    }

    public ItemCapabilityProvider(ItemStack itemStack, IEnergyItem item) {
        this.itemStack = itemStack;
        this.item = item;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability) {
        if (capability == CapabilityEnergy.ENERGY) {
            return energy.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public long getStoredPower() {
        return item.getEnergyStoredL(itemStack);
    }

    @Override
    public long getCapacity() {
        return item.getMaxEnergyStoredL(itemStack);
    }
}
