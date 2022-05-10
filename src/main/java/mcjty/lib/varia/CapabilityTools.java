package mcjty.lib.varia;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import mcjty.lib.fabric.TransferHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public class CapabilityTools {

    @Nonnull
    public static LazyOptional<Storage<ItemVariant>> getItemCapabilitySafe(BlockEntity tileEntity) {
        if (tileEntity == null) {
            return LazyOptional.empty();
        }
        try {
            return TransferHelper.getItemStorage(tileEntity);
        } catch (RuntimeException e) {
            reportWrongBlock(tileEntity, e);
            return LazyOptional.empty();
        }
    }

    @Nonnull
    public static LazyOptional<Storage<FluidVariant>> getFluidCapabilitySafe(BlockEntity tileEntity) {
        if (tileEntity == null) {
            return LazyOptional.empty();
        }
        try {
            return TransferHelper.getFluidStorage(tileEntity);
        } catch (RuntimeException e) {
            reportWrongBlock(tileEntity, e);
            return LazyOptional.empty();
        }
    }

    private static void reportWrongBlock(BlockEntity tileEntity, Exception e) {
        if (tileEntity != null) {
            ResourceLocation name = Registry.BLOCK.getKey(tileEntity.getLevel().getBlockState(tileEntity.getBlockPos()).getBlock());
            Logging.logError("Block " + name.toString() + " at " + BlockPosTools.toString(tileEntity.getBlockPos()) + " does not respect the capability API and crashes on null side.");
            Logging.logError("Please report to the corresponding mod. This is not a bug in RFTools!");
        }
        if (e != null) {
            Logging.logError("Exception", e);
        }
    }
}
