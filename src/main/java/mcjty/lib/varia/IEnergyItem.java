package mcjty.lib.varia;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

public interface IEnergyItem {
    long receiveEnergyL(ItemStack container, long maxReceive, TransactionContext tx);

    long extractEnergyL(ItemStack container, long maxExtract, TransactionContext tx);

    long getEnergyStoredL(ItemStack container);

    long getMaxEnergyStoredL(ItemStack container);

    default int receiveEnergy(ItemStack container, int maxReceive, TransactionContext tx) {
        return (int)receiveEnergyL(container, maxReceive, tx);
    }

    default int extractEnergy(ItemStack container, int maxExtract, TransactionContext tx) {
        return (int)extractEnergyL(container, maxExtract, tx);
    }

    default int getEnergyStored(ItemStack container) {
        return EnergyTools.getIntEnergyStored(getEnergyStoredL(container), getMaxEnergyStoredL(container));
    }

    default int getMaxEnergyStored(ItemStack container) {
        return EnergyTools.unsignedClampToInt(getMaxEnergyStoredL(container));
    }
}
