package mcjty.lib.container;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import mcjty.lib.fabric.TransferHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * This inventory locator serves as a cache and a way to quickly items in an adjacent
 * inventory. If the inventory is full it will throw out the items in the world.
 */
public class InventoryLocator {

    private BlockPos inventoryCoordinate = null;
    private Direction inventorySide = null;

    @Nonnull
    private LazyOptional<Storage<ItemVariant>> getItemHandlerAtDirection(Level worldObj, BlockPos thisCoordinate, Direction direction) {
        if (direction == null) {
            if (inventoryCoordinate != null) {
                return getItemHandlerAtCoordinate(worldObj, inventoryCoordinate, inventorySide);
            }
            return LazyOptional.empty();
        }
        BlockEntity te = worldObj.getBlockEntity(thisCoordinate);
        if (te == null) {
            return LazyOptional.empty();
        }
        return TransferHelper.getItemStorage(te, direction.getOpposite()).map(handler -> {
            // Remember in inventoryCoordinate (acts as a cache)
            inventoryCoordinate = thisCoordinate.relative(direction);
            inventorySide = direction.getOpposite();
            return getItemHandlerAtCoordinate(worldObj, inventoryCoordinate, inventorySide);
        }).orElse(LazyOptional.empty());
    }

    @Nonnull
    private LazyOptional<Storage<ItemVariant>> getItemHandlerAtCoordinate(Level worldObj, BlockPos c, Direction direction) {
        BlockEntity te = worldObj.getBlockEntity(c);
        if (te == null) {
            return LazyOptional.empty();
        }
        return TransferHelper.getItemStorage(te, direction);
    }


    public void ejectStack(Level worldObj, BlockPos pos, ItemStack stack, BlockPos thisCoordinate, Direction[] directions) {
        for (Direction dir : directions) {
            if (stack.isEmpty()) {
                break;
            }
            ItemStack finalStack = stack;
            stack = getItemHandlerAtDirection(worldObj, thisCoordinate, dir).map(handler -> TransferHelper.quickInsert(handler, finalStack, false)).orElse(stack);
        }

        if (!stack.isEmpty()) {
            ItemEntity entityItem = new ItemEntity(worldObj, pos.getX(), pos.getY(), pos.getZ(), stack);
            worldObj.addFreshEntity(entityItem);
        }
    }


    public Direction getInventorySide() {
        return inventorySide;
    }

}
