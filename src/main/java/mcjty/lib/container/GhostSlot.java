package mcjty.lib.container;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * A slot typically used for crafting grids.
 */
public class GhostSlot extends SlotItemHandler {

    public GhostSlot(ItemStackHandler inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public void set(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setCount(1);
        }
        super.set(stack);
    }
}
