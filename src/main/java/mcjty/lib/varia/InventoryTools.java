package mcjty.lib.varia;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import mcjty.lib.fabric.TransferHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InventoryTools {
    /**
     * Get the size of the inventory
     */
    public static int getInventorySize(BlockEntity tileEntity) {
        if (tileEntity == null) {
            return 0;
        }

        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(IItemHandler::getSlots).orElse(0);
    }

    public static boolean isInventory(BlockEntity te) {
        return te != null && TransferHelper.getItemStorage(te).isPresent();
    }

    /**
     * Return a stream of items in an inventory matching the predicate
     */
    public static Stream<ResourceAmount<ItemVariant>> getItems(BlockEntity tileEntity, Predicate<ResourceAmount<ItemVariant>> predicate) {
        Stream.Builder<ResourceAmount<ItemVariant>> builder = Stream.builder();

        if (tileEntity != null) {
            TransferHelper.getItemStorage(tileEntity).ifPresent(handler -> {
                try (Transaction t = TransferUtil.getTransaction()) {
                    handler.iterable(t).forEach(view -> {
                        ResourceAmount<ItemVariant> resourceAmount = new ResourceAmount<>(view.getResource(), view.getAmount());
                        if (!view.isResourceBlank() && predicate.test(resourceAmount)) {
                            builder.add(resourceAmount);
                        }
                    });
                }
            });
        }
        return builder.build();
    }

    /**
     * Return the first item in an inventory matching the predicate
     */
    @Nonnull
    public static ResourceAmount<ItemVariant> getFirstMatchingItem(BlockEntity tileEntity, Predicate<ResourceAmount<ItemVariant>> predicate) {
        if (tileEntity != null) {
            return TransferHelper.getItemStorage(tileEntity).map(handler -> {
                try (Transaction t = TransferUtil.getTransaction()) {
                    for (StorageView<ItemVariant> view : handler.iterable(t)) {
                        ResourceAmount<ItemVariant> resourceAmount = new ResourceAmount<>(view.getResource(), view.getAmount());
                        if (!view.isResourceBlank() && predicate.test(resourceAmount)) {
                            return resourceAmount;
                        }
                    }
                }
                return new ResourceAmount<>(ItemVariant.blank(), 0L);
            }).orElse(new ResourceAmount<>(ItemVariant.blank(), 0L));
        }
        return new ResourceAmount<>(ItemVariant.blank(), 0L);
    }

    /**
     * Insert an item into an inventory at the given direction. Supports IItemHandler as
     * well as IInventory. Returns an itemstack with whatever could not be inserted or empty item
     * on succcess.
     */
    @Nonnull
    public static ItemStack insertItem(Level world, BlockPos pos, Direction direction, @Nonnull ItemStack s) {
        BlockEntity te = world.getBlockEntity(direction == null ? pos : pos.relative(direction));
        if (te != null) {
            Direction opposite = direction == null ? null : direction.getOpposite();
            return TransferHelper.getItemStorage(te, opposite)
                    .map(handler -> TransferHelper.quickInsert(handler, s, false))
                    .orElse(ItemStack.EMPTY);
        }
        return s;
    }

    public static boolean isItemStackConsideredEqual(ItemStack result, ItemStack itemstack1) {
        // @todo 1.14
//        return !itemstack1.isEmpty() && itemstack1.getItem() == result.getItem() && (!result.getHasSubtypes() || result.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(result, itemstack1);
        return !itemstack1.isEmpty() && itemstack1.getItem() == result.getItem() && (result.getDamageValue() == itemstack1.getDamageValue()) && ItemStack.tagMatches(result, itemstack1);
    }

    @Nonnull
    public static ItemStack insertItemRanged(Storage<ItemVariant> dest, @Nonnull ItemStack stack, int start, int stop, boolean simulate) {
        if (dest == null || stack.isEmpty())
            return stack;

        for (int i = start; i < stop; i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }
}
