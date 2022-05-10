package mcjty.lib.api.container;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;

import javax.annotation.Nullable;

public interface IGenericContainer {

    void addShortListener(DataSlot holder);

    void addIntegerListener(DataSlot holder);

    void addContainerDataListener(IContainerDataListener dataListener);

    void setupInventories(@Nullable Storage<ItemVariant> itemHandler, Inventory inventory);

    AbstractContainerMenu getAsContainer();
}
