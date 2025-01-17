package mcjty.lib.container;

import com.google.common.collect.Range;
import io.github.fabricators_of_create.porting_lib.extensions.SlotExtensions;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mcjty.lib.McJtyLib;
import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.IContainerDataListener;
import mcjty.lib.api.container.IGenericContainer;
import mcjty.lib.network.PacketContainerDataToClient;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Generic container support
 */
public class GenericContainer extends AbstractContainerMenu implements IGenericContainer {
    protected final Map<String, ItemStackHandler> inventories = new HashMap<>();
    private final Map<ResourceLocation, IContainerDataListener> containerData = new HashMap<>();
    private final ContainerFactory factory;
    protected final BlockPos pos;
    protected final GenericTileEntity te;
    private final List<DataSlot> intReferenceHolders = new ArrayList<>();
    private boolean doForce = true;
    private final Player player;

    public GenericContainer(@Nullable MenuType<?> type, int id, ContainerFactory factory, BlockPos pos, @Nullable GenericTileEntity te, @Nonnull Player player) {
        super(type, id);
        this.factory = factory;
        this.pos = pos;
        this.te = te;
        this.player = player;
    }

    public GenericContainer(@Nonnull Supplier<MenuType<GenericContainer>> type, int id, @Nonnull Supplier<ContainerFactory> factory, @Nullable GenericTileEntity te,
                            @Nonnull Player player) {
        super(type.get(), id);
        this.factory = factory.get();
        this.pos = te.getBlockPos();
        this.te = te;
        this.player = player;
    }

    @Override
    public AbstractContainerMenu getAsContainer() {
        return this;
    }

    public GenericTileEntity getTe() {
        return te;
    }

    public Player getPlayer() {
        return player;
    }

    @Nonnull
    @Override
    protected DataSlot addDataSlot(@Nonnull DataSlot holder) {
        intReferenceHolders.add(holder);
        return super.addDataSlot(holder);
    }

    @Override
    public void addShortListener(DataSlot holder) {
        addDataSlot(holder);
    }

    @Override
    public void addIntegerListener(DataSlot holder) {
        addDataSlot(new DataSlot() {
            private int lastKnown;

            @Override
            public int get() {
                return holder.get() & 0xffff;
            }

            @Override
            public void set(int val) {
                int full = holder.get();
                holder.set((full & 0xffff0000) | (val & 0xffff));
            }

            @Override
            public boolean checkAndClearUpdateFlag() {
                int i = this.get();
                boolean flag = i != this.lastKnown;
                this.lastKnown = i;
                return flag;
            }
        });
        addDataSlot(new DataSlot() {
            private int lastKnown;

            @Override
            public int get() {
                return (holder.get() >> 16) & 0xffff;
            }

            @Override
            public void set(int val) {
                int full = holder.get();
                holder.set((full & 0x0000ffff) | ((val & 0xffff) << 16));
            }

            @Override
            public boolean checkAndClearUpdateFlag() {
                int i = this.get();
                boolean flag = i != this.lastKnown;
                this.lastKnown = i;
                return flag;
            }
        });
    }

    @Override
    public void addContainerDataListener(IContainerDataListener data) {
        this.containerData.put(data.getId(), data);
    }

    public void addInventory(String name, @Nullable Storage<ItemVariant> inventory) {
        if (inventory != null) {
            inventories.put(name, inventory);
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public Storage<ItemVariant> getInventory(String name) {
        return inventories.get(name);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        if (te == null || te.canPlayerAccess(player)) {
            return true;
        }
        return false;
    }

    public SlotType getSlotType(int index) {
        return factory.getSlotType(index);
    }

    @Nullable
    public Slot getSlotByInventoryAndIndex(String name, int index) {
        Storage<ItemVariant> inv = inventories.get(name);
        if (inv == null) {
            return null;
        }
        for (Slot slot : slots) {
            if (slot instanceof SlotItemHandler) {
                ItemStackHandler itemHandler = ((SlotItemHandler) slot).getItemHandler();
                if (itemHandler == inv && ((SlotExtensions)slot).getSlotIndex() == index) {
                    return slot;
                }
            }
        }
        return null;
    }

    @Override
    public void setupInventories(Storage<ItemVariant> itemHandler, Inventory inventory) {
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, PlayerInventoryStorage.of(inventory));
        generateSlots(inventory.player);
    }

    public void generateSlots(Player player) {
        for (SlotFactory slotFactory : factory.getSlots()) {
            ItemStackHandler inventory = inventories.get(slotFactory.inventoryName());
            int index = slotFactory.index();
            int x = slotFactory.x();
            int y = slotFactory.y();
            SlotType slotType = slotFactory.getSlotType();
            Slot slot = createSlot(slotFactory, player, inventory, index, x, y, slotType);
            addSlot(slot);
        }
    }

    protected Slot createSlot(SlotFactory slotFactory, Player playerEntity, final ItemStackHandler inventory, final int index, final int x, final int y, SlotType slotType) {
        Slot slot;
        if (slotType == SlotType.SLOT_GHOST) {
            slot = new GhostSlot(inventory, index, x, y);
        } else if (slotType == SlotType.SLOT_GHOSTOUT) {
            slot = new GhostOutputSlot(inventory, index, x, y);
        } else if (slotType == SlotType.SLOT_SPECIFICITEM) {
            final SlotDefinition slotDefinition = slotFactory.slotDefinition();
            slot = new SlotItemHandler(inventory, index, x, y) {
                @Override
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    return slotDefinition.itemStackMatches(stack);
                }
            };
        } else if (slotType == SlotType.SLOT_CRAFTRESULT) {
            slot = new CraftingSlot(playerEntity, inventory, te, index, x, y)
                    .onCraft(slotFactory.slotDefinition().getOnCraft());
        } else {
            slot = new BaseSlot(inventory, te, index, x, y);
        }
        return slot;
    }

    private boolean mergeItemStacks(ItemStack itemStack, SlotType slotType, boolean reverse) {
        if (slotType == SlotType.SLOT_SPECIFICITEM) {
            return mergeItemStacks(itemStack, definition -> definition.isSpecific() && definition.itemStackMatches(itemStack), reverse);
        } else {
            return mergeItemStacks(itemStack, definition -> definition.getType() == slotType, reverse);
        }
    }

    private boolean mergeItemStacks(ItemStack itemStack, Predicate<SlotDefinition> slotType, boolean reverse) {
        SlotRanges ranges = factory.getRanges(slotType);
        Set<Range<Integer>> set = ranges.asRanges();
        if (set.isEmpty()) {
            return false;
        }
        for (Range<Integer> r : ranges.asRanges()) {
            Integer start = r.lowerEndpoint();
            int end = r.upperEndpoint();
            if (moveItemStackTo(itemStack, start, end, reverse)) {
                return true;
            }
        }
        return false;
    }

//    private boolean mergeItemStacks(ItemStack itemStack, SlotDefinition slotDefinition, boolean reverse) {
//        SlotRanges ranges = factory.getSlotRangesMap().get(slotDefinition);
//        if (ranges == null) {
//            return false;
//        }
//
//        SlotType slotType = slotDefinition.getType();
//
//        if (itemStack.getItem() != null && slotType == SlotType.SLOT_SPECIFICITEM && !slotDefinition.itemStackMatches(itemStack)) {
//            return false;
//        }
//        for (Range<Integer> r : ranges.asRanges()) {
//            Integer start = r.lowerEndpoint();
//            int end = r.upperEndpoint();
//            if (mergeItemStack(itemStack, start, end, reverse)) {
//                return true;
//            }
//        }
//        return false;
//    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack origStack = slot.getItem();
            itemstack = origStack.copy();

            if (factory.isSpecificItemSlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERINV, true)) {
                    if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERHOTBAR, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                slot.onQuickCraft(origStack, itemstack);
            } else if (factory.isOutputSlot(index) || factory.isInputSlot(index) || factory.isGenericSlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_SPECIFICITEM, false)) {
                    if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERINV, true)) {
                        if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERHOTBAR, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                slot.onQuickCraft(origStack, itemstack);
            } else if (factory.isGhostSlot(index) || factory.isGhostOutputSlot(index)) {
                return ItemStack.EMPTY;
            } else if (factory.isPlayerInventorySlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_SPECIFICITEM, false)) {
                    if (!mergeItemStacks(origStack, SlotDefinition::isInput, false)) {
                        if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERHOTBAR, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else if (factory.isPlayerHotbarSlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_SPECIFICITEM, false)) {
                    if (!mergeItemStacks(origStack, SlotDefinition::isInput, false)) {
                        if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERINV, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else {
                Logging.log("Weird slot at index: " + index);
            }

            if (origStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (origStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, origStack);
        }

        return itemstack;
    }


    @Override
    protected boolean moveItemStackTo(@Nonnull ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reverseOrder) {
        boolean result = false;
        int checkIndex = fromIndex;

        if (reverseOrder) {
            checkIndex = toIndex - 1;
        }

        Slot slot;
        ItemStack itemstack1 = ItemStack.EMPTY;

        if (par1ItemStack.isStackable()) {

            while (!par1ItemStack.isEmpty() && (!reverseOrder && checkIndex < toIndex || reverseOrder && checkIndex >= fromIndex)) {
                slot = this.slots.get(checkIndex);
                itemstack1 = slot.getItem();

                if (!itemstack1.isEmpty() && itemstack1.getItem() == par1ItemStack.getItem() && (par1ItemStack.getDamageValue() == itemstack1.getDamageValue())
                        && ItemStack.tagMatches(par1ItemStack, itemstack1) && slot.mayPlace(par1ItemStack)) {

                    int mergedSize = itemstack1.getCount() + par1ItemStack.getCount();
                    int maxStackSize = Math.min(par1ItemStack.getMaxStackSize(), slot.getMaxStackSize());
                    if (mergedSize <= maxStackSize) {
                        par1ItemStack.setCount(0);
                        itemstack1.setCount(Math.max(mergedSize, 0));
                        slot.setChanged();
                        result = true;
                    } else if (itemstack1.getCount() < maxStackSize) {
                        int amount = -(maxStackSize - itemstack1.getCount());
                        par1ItemStack.grow(amount);
                        itemstack1.setCount(Math.max(maxStackSize, 0));
                        slot.setChanged();
                        result = true;
                    }
                }

                if (reverseOrder) {
                    --checkIndex;
                } else {
                    ++checkIndex;
                }
            }
        }

        if (!par1ItemStack.isEmpty()) {
            if (reverseOrder) {
                checkIndex = toIndex - 1;
            } else {
                checkIndex = fromIndex;
            }

            while (!reverseOrder && checkIndex < toIndex || reverseOrder && checkIndex >= fromIndex) {
                slot = this.slots.get(checkIndex);
                itemstack1 = slot.getItem();

                if (itemstack1.isEmpty() && slot.mayPlace(par1ItemStack)) {
                    ItemStack in = par1ItemStack.copy();
                    int amount1 = Math.min(in.getCount(), slot.getMaxStackSize());
                    in.setCount(Math.max(amount1, 0));

                    slot.set(in);
                    slot.setChanged();
                    if (in.getCount() >= par1ItemStack.getCount()) {
                        par1ItemStack.setCount(0);
                    } else {
                        int amount = -in.getCount();
                        par1ItemStack.grow(amount);
                    }
                    result = true;
                    break;
                }

                if (reverseOrder) {
                    --checkIndex;
                } else {
                    ++checkIndex;
                }
            }
        }

        return result;
    }

    @Override
    public void clicked(int index, int button, @Nonnull ClickType mode, @Nonnull Player player) {
        if (factory.isGhostSlot(index)) {
            Slot slot = getSlot(index);
            if (slot.hasItem()) {
                slot.set(ItemStack.EMPTY);
            }

            ItemStack clickedWith = getCarried();
            if (!clickedWith.isEmpty()) {
                ItemStack copy = clickedWith.copy();
                copy.setCount(1);
                slot.set(copy);
            }
            broadcastChanges();
        } else {
            super.clicked(index, button, mode, player);
        }
    }

    public IContainerDataListener getListener(ResourceLocation id) {
        return containerData.get(id);
    }

    private void broadcast() {
        for (int i = 0; i < intReferenceHolders.size(); i++) {
            DataSlot holder = intReferenceHolders.get(i);
            for (ContainerListener listener : this.containerListeners) {
                listener.dataChanged(this, i, holder.get());
            }
        }
        if (player instanceof ServerPlayer serverPlayer) {
            for (IContainerDataListener data : containerData.values()) {
                ByteBuf newbuf = Unpooled.buffer();
                FriendlyByteBuf buffer = new FriendlyByteBuf(newbuf);
                data.toBytes(buffer);
                PacketContainerDataToClient packet = new PacketContainerDataToClient(data.getId(), buffer);
                McJtyLib.networkHandler.sendTo(packet, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    public void forceBroadcast() {
        this.doForce = true;
    }

    @Override
    public void broadcastChanges() {
        if (doForce) {
            broadcast();
            doForce = false;
        }

        super.broadcastChanges();

        if (player instanceof ServerPlayer serverPlayer) {
            for (IContainerDataListener data : containerData.values()) {
                if (data.isDirtyAndClear()) {
                    ByteBuf newbuf = Unpooled.buffer();
                    FriendlyByteBuf buffer = new FriendlyByteBuf(newbuf);
                    data.toBytes(buffer);
                    PacketContainerDataToClient packet = new PacketContainerDataToClient(data.getId(), buffer);
                    McJtyLib.networkHandler.sendTo(packet, serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        }
    }

    public static MenuType<AbstractContainerMenu> createContainerType(String registryName) {
        MenuType<AbstractContainerMenu> containerType = new ExtendedScreenHandlerType<>(((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            BlockEntity te = inv.player.getCommandSenderWorld().getBlockEntity(pos);
            if (te == null) {
                throw new IllegalStateException("Something went wrong getting the GUI");
            }
            return te.getCapability(CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY).map(h -> Objects.requireNonNull(h.createMenu(windowId, inv, inv.player))).orElseThrow(RuntimeException::new);
        }));
        return Registry.register(Registry.MENU, registryName, containerType);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> createContainerType() {
        MenuType<AbstractContainerMenu> containerType = new ExtendedScreenHandlerType<>(((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            BlockEntity te = inv.player.getCommandSenderWorld().getBlockEntity(pos);
            if (te == null) {
                throw new IllegalStateException("Something went wrong getting the GUI");
            }
            return te.getCapability(CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY).map(h -> Objects.requireNonNull(h.createMenu(windowId, inv, inv.player))).orElseThrow(RuntimeException::new);
        }));
        return (MenuType<T>) containerType;
    }

    public static <T extends GenericContainer, E extends GenericTileEntity> MenuType<T> createRemoteContainerType(
            BiFunction<ResourceKey<Level>, BlockPos, E> dummyTEFactory,
            ContainerSupplier<T, E> containerFactory, int slots) {
        return new ExtendedScreenHandlerType<>(((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();

            E te = dummyTEFactory.apply(LevelTools.getId(data.readResourceLocation()), pos);
            CompoundTag compound = data.readNbt();
            te.load(compound);

            T container = containerFactory.create(windowId, pos, te, inv.player);
            container.setupInventories(new ItemStackHandler(slots), inv);
            return container;
        }));
    }

    public interface ContainerSupplier<T extends GenericContainer, E extends GenericTileEntity> {
        T create(int windowId, BlockPos pos, E te, Player player);
    }
}
