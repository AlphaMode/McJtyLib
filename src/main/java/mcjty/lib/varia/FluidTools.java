package mcjty.lib.varia;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import mcjty.lib.fabric.TransferHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class FluidTools {

    /**
     * Make sure the forge bucket is enabled. If needed do this in your mod constructor:
     * FluidRegistry.enableUniversalBucket();
     */
    @Nonnull
    public static ItemStack convertFluidToBucket(@Nonnull FluidStack fluidStack) {
        //                return FluidContainerRegistry.fillFluidContainer(fluidStack, new ItemStack(Items.BUCKET));
        Item bucket = fluidStack.getFluid().getBucket();
        return bucket != null ? new ItemStack(bucket) : ItemStack.EMPTY;
    }

    @Nonnull
    public static FluidStack convertBucketToFluid(@Nonnull ItemStack bucket) {
        return TransferUtil.getFluidContained(bucket).orElse(FluidStack.EMPTY);
    }


    public static boolean isEmptyContainer(@Nonnull ItemStack itemStack) {
        return TransferHelper.getFluidStorage(itemStack).map(handler -> {
            try (Transaction t = TransferUtil.getTransaction()) {
                for (StorageView<FluidVariant> view : handler.iterable(t)) {
                    if (view.getCapacity() > 0) {
                        if (view.isResourceBlank()) {
                            return true;
                        } else if (view.getAmount() > 0) {
                            return false;
                        }
                    }
                }
            }
            return false;
        }).orElse(false);
    }

    public static boolean isFilledContainer(@Nonnull ItemStack itemStack) {
        return FluidUtil.getFluidHandler(itemStack).map(handler -> {
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack contents = handler.getFluidInTank(i);
                if (contents.isEmpty() || contents.getAmount() < handler.getTankCapacity(i)) {
                    return false;
                }
            }
            return true;
        }).orElse(false);
    }

    // Drain a fluid container and return an empty container
    @Nonnull
    public static ItemStack drainContainer(@Nonnull ItemStack container) {
        ItemStack empty = container.copy();
        empty.setCount(1);
        return FluidUtil.getFluidHandler(empty).map(handler -> {
            if (!handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE).isEmpty()) {
                return handler.getContainer();
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }

    // Fill a container with a fluid and return the filled container
    @Nonnull
    public static ItemStack fillContainer(@Nonnull FluidStack fluidStack, @Nonnull ItemStack itemStack) {
        return FluidUtil.getFluidHandler(itemStack.copy()).map(handler -> {
            int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            if (filled == 0) {
                return ItemStack.EMPTY;
            }
            return handler.getContainer();
        }).orElse(ItemStack.EMPTY);
    }

    @Nonnull
    public static FluidStack pickupFluidBlock(Level world, BlockPos pos, @Nonnull Predicate<FluidStack> action, @Nonnull Runnable clearBlock) {
        BlockState blockstate = world.getBlockState(pos);
        FluidState fluidstate = world.getFluidState(pos);
        Material material = blockstate.getMaterial();
        Fluid fluid = fluidstate.getType();

        if (blockstate.getBlock() instanceof BucketPickup && fluid != Fluids.EMPTY) {
            FluidStack stack = new FluidStack(fluid, FluidConstants.BUCKET);
            if (action.test(stack)) {
                ItemStack fluidBucket = ((BucketPickup) blockstate.getBlock()).pickupBlock(world, pos, blockstate);
                return FluidUtil.getFluidContained(fluidBucket).map(f -> new FluidStack(f, FluidConstants.BUCKET)).orElse(FluidStack.EMPTY);
            }
            return stack;
        } else if (blockstate.getBlock() instanceof LiquidBlock) {
            FluidStack stack = new FluidStack(fluid, FluidConstants.BUCKET);
            if (action.test(stack)) {
                clearBlock.run();
            }
            return stack;
        } else if (material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
            FluidStack stack = new FluidStack(Fluids.WATER, FluidConstants.BUCKET);
            if (action.test(stack)) {
                BlockEntity tileentity = blockstate.getBlock() instanceof EntityBlock ? world.getBlockEntity(pos) : null;
                Block.dropResources(blockstate, world, pos, tileentity);
                clearBlock.run();
            }
            return stack;
        }
        return FluidStack.EMPTY;
    }
}
