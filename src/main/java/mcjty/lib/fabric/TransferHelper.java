package mcjty.lib.fabric;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemItemStorages;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class TransferHelper {
    public static LazyOptional<EnergyStorage> getEnergyStorage(BlockEntity be, @Nullable Direction side) {
        if (side == null) {
            for (Direction dir : Direction.values()) {
                EnergyStorage storage = EnergyStorage.SIDED.find(be.getLevel(), be.getBlockPos(), side);
                if (storage != null)
                    return LazyOptional.ofObject(storage);
            }
            return LazyOptional.empty();
        }
        return LazyOptional.ofObject(EnergyStorage.SIDED.find(be.getLevel(), be.getBlockPos(), side));
    }

    public static LazyOptional<EnergyStorage> getEnergyStorage(BlockEntity be) {
        return getEnergyStorage(be, null);
    }

    public static LazyOptional<EnergyStorage> getEnergyStorage(ItemStack stack) {
        return LazyOptional.ofObject(ContainerItemContext.withInitial(stack).find(EnergyStorage.ITEM));
    }

    public static long quickInsert(EnergyStorage storage, long amount, boolean sim) {
        try (Transaction t = TransferUtil.getTransaction()) {
             long inserted = storage.insert(amount, t);
             if (!sim)
                 t.commit();
             return inserted;
        }
    }

    public static <R> long quickInsert(Storage<R> storage, ResourceAmount<R> resource, boolean sim) {
        try (Transaction t = TransferUtil.getTransaction()) {
            long inserted = storage.insert(resource.resource(), resource.amount(), t);
            if (!sim)
                t.commit();
            return inserted;
        }
    }

    public static ItemStack quickInsert(Storage<ItemVariant> storage, ItemStack stack, boolean sim) {
        long inserted = quickInsert(storage, new ResourceAmount<>(ItemVariant.of(stack), stack.getCount()), sim);
        stack.setCount((int) inserted);
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        return stack;
    }

    public static LazyOptional<Storage<ItemVariant>> getItemStorage(BlockEntity be, @Nullable Direction side) {
        return LazyOptional.ofObject(TransferUtil.getItemStorage(be, side));
    }

    public static LazyOptional<Storage<ItemVariant>> getItemStorage(BlockEntity be) {
        return LazyOptional.ofObject(TransferUtil.getItemStorage(be, null));
    }

    public static LazyOptional<Storage<ItemVariant>> getItemStorage(ItemStack stack) {
        return LazyOptional.ofObject(ContainerItemContext.withInitial(stack).find(ItemItemStorages.ITEM));
    }

    public static LazyOptional<Storage<FluidVariant>> getFluidStorage(BlockEntity be, @Nullable Direction side) {
        return LazyOptional.ofObject(TransferUtil.getFluidStorage(be, side));
    }

    public static LazyOptional<Storage<FluidVariant>> getFluidStorage(BlockEntity be) {
        return LazyOptional.ofObject(TransferUtil.getFluidStorage(be, null));
    }

    public static LazyOptional<Storage<FluidVariant>> getFluidStorage(ItemStack stack) {
        return LazyOptional.ofObject(ContainerItemContext.withInitial(stack).find(FluidStorage.ITEM));
    }
}
