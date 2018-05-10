package mcjty.lib.container;

import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BaseSlot extends Slot {
    public BaseSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void putStack(ItemStack stack) {
        if (inventory instanceof GenericTileEntity) {
            GenericTileEntity genericTileEntity = (GenericTileEntity) inventory;
            genericTileEntity.onSlotChanged(getSlotIndex(), stack);
        }
        super.putStack(stack);
    }
}
