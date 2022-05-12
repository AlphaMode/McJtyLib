//package mcjty.lib.container;
//
//import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
//import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
//import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
//import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.items.IItemHandlerModifiable;
//
//import javax.annotation.Nonnull;
//import java.util.HashMap;
//import java.util.Map;
//
//public class UndoableItemHandler implements Storage<ItemVariant> {
//
//    private final IItemHandlerModifiable handler;
//    private final Map<Integer, ResourceAmount<ItemVariant>> undo = new HashMap<>();
//
//    public UndoableItemHandler(IItemHandlerModifiable handler) {
//        this.handler = handler;
//    }
//
//    public void remember(int slot) {
//        if (!undo.containsKey(slot)) {
//            undo.put(slot, handler.getStackInSlot(slot).copy());
//        }
//    }
//
//    public void restore() {
//        for (Map.Entry<Integer, ItemStack> entry : undo.entrySet()) {
//            handler.setStackInSlot(entry.getKey(), entry.getValue());
//        }
//        undo.clear();
//    }
//
//    @Override
//    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
//        remember(slot);
//        handler.setStackInSlot(slot, stack);
//    }
//
//    @Override
//    public int getSlots() {
//        return handler.getSlots();
//    }
//
//    @Nonnull
//    @Override
//    public ItemStack getStackInSlot(int slot) {
//        return handler.getStackInSlot(slot);
//    }
//
//    @Nonnull
//    @Override
//    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
//        if (!simulate) {
//            remember(slot);
//        }
//        return handler.insertItem(slot, stack, simulate);
//    }
//
//    @Nonnull
//    @Override
//    public ItemStack extractItem(int slot, int amount, boolean simulate) {
//        if (!simulate) {
//            remember(slot);
//        }
//        return handler.extractItem(slot, amount, simulate);
//    }
//
//    @Override
//    public int getSlotLimit(int slot) {
//        return handler.getSlotLimit(slot);
//    }
//
//    @Override
//    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
//        return handler.isItemValid(slot, stack);
//    }
//}
