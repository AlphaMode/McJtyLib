package mcjty.lib.network;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.Logging;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NetworkTools {

    public static FluidStack readFluidStack(ByteBuf dataIn) {
        FriendlyByteBuf buf = new FriendlyByteBuf(dataIn);
        CompoundTag nbt = buf.readNbt();
        return FluidStack.loadFluidStackFromNBT(nbt);
    }

    public static void writeFluidStack(ByteBuf dataOut, FluidStack fluidStack) {
        FriendlyByteBuf buf = new FriendlyByteBuf(dataOut);
        CompoundTag nbt = new CompoundTag();
        fluidStack.writeToNBT(nbt);
        try {
            buf.writeNbt(nbt);
        } catch (RuntimeException e) {
            Logging.logError("Error writing fluid stack", e);
        }
    }

    public static String readStringUTF8(FriendlyByteBuf dataIn) {
        if (!dataIn.readBoolean()) {
            return null;
        }
        return dataIn.readUtf(32767);
    }

    public static void writeStringUTF8(FriendlyByteBuf dataOut, String str) {
        if (str == null) {
            dataOut.writeBoolean(false);
            return;
        }
        dataOut.writeBoolean(true);
        dataOut.writeUtf(str);
    }

    public static void writeStringList(FriendlyByteBuf dataOut, @Nonnull List<String> list) {
        dataOut.writeInt(list.size());
        list.forEach(s -> writeStringUTF8(dataOut, s));
    }

    @Nonnull
    public static List<String> readStringList(FriendlyByteBuf dataIn) {
        int size = dataIn.readInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            list.add(readStringUTF8(dataIn));
        }
        return list;
    }

    /// This function supports itemstacks with more then 64 items.
    public static ItemStack readItemStack(FriendlyByteBuf buf) {
        CompoundTag nbt = buf.readNbt();
        ItemStack stack = ItemStack.of(nbt);
        stack.setCount(buf.readInt());
        return stack;
    }

    /// This function supports itemstacks with more then 64 items.
    public static void writeItemStack(FriendlyByteBuf buf, ItemStack itemStack) {
        CompoundTag nbt = new CompoundTag();
        itemStack.save(nbt);
        try {
            buf.writeNbt(nbt);
            buf.writeInt(itemStack.getCount());
        } catch (Exception e) {
            Logging.logError("Error", e);
        }
    }


    public static <T extends Enum<T>> void writeEnum(ByteBuf buf, T value, T nullValue) {
        if (value == null) {
            buf.writeInt(nullValue.ordinal());
        } else {
            buf.writeInt(value.ordinal());
        }
    }

    public static <T extends Enum<T>> T readEnum(ByteBuf buf, T[] values) {
        return values[buf.readInt()];
    }

    public static <T extends Enum<T>> void writeEnumCollection(ByteBuf buf, Collection<T> collection) {
        buf.writeInt(collection.size());
        for (T type : collection) {
            buf.writeInt(type.ordinal());
        }
    }

    public static <T extends Enum<T>> void readEnumCollection(ByteBuf buf, Collection<T> collection, T[] values) {
        collection.clear();
        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            collection.add(values[buf.readInt()]);
        }
    }

    @Nonnull
    public static List<ItemStack> readItemStackList(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<ItemStack> outputs = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            outputs.add(buf.readItem());
        }
        return outputs;
    }

    public static void writeItemStackList(FriendlyByteBuf buf, @Nonnull List<ItemStack> outputs) {
        buf.writeInt(outputs.size());
        for (ItemStack output : outputs) {
            buf.writeItem(output);
        }
    }

    public static void writeBlockPosList(FriendlyByteBuf dataOut, @Nonnull List<BlockPos> list) {
        dataOut.writeInt(list.size());
        list.forEach(dataOut::writeBlockPos);
    }

    @Nonnull
    public static List<BlockPos> readBlockPosList(FriendlyByteBuf dataIn) {
        int size = dataIn.readInt();
        List<BlockPos> list = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            list.add(dataIn.readBlockPos());
        }
        return list;
    }


}
