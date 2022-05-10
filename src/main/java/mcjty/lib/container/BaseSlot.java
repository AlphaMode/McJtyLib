package mcjty.lib.container;

import io.github.fabricators_of_create.porting_lib.extensions.SlotExtensions;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class BaseSlot extends SlotItemHandler implements SlotExtensions {

    private final GenericTileEntity te;

    public BaseSlot(ItemStackHandler inventory, GenericTileEntity te, int index, int x, int y) {
        super(inventory, index, x, y);
        this.te = te;
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        if (te != null) {
            te.onSlotChanged(getSlotIndex(), stack);
        }
        super.set(stack);
    }

    public GenericTileEntity getTe() {
        return te;
    }
}
