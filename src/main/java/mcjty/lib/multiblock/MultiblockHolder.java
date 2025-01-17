package mcjty.lib.multiblock;

import net.minecraft.nbt.CompoundTag;

public class MultiblockHolder<T extends IMultiblock> {

    private final T mb;
//    private final Set<BlockPos> positions = new HashSet<>();

    public MultiblockHolder(T mb) {
        this.mb = mb;
    }

    public T getMb() {
        return mb;
    }

//    public Set<BlockPos> getPositions() {
//        return positions;
//    }

    public void load(CompoundTag tagCompound) {
//        positions.clear();
//        ListNBT lst = tagCompound.getList("pos", Constants.NBT.TAG_LONG);
//        for (INBT nbt : lst) {
//            positions.add(BlockPos.of(((LongNBT)nbt).getAsLong()));
//        }
    }

    public CompoundTag save(CompoundTag tagCompound) {
//        ListNBT list = new ListNBT();
//        positions.forEach(p -> list.add(LongNBT.valueOf(p.asLong())));
//        tagCompound.put("pos", list);
        return tagCompound;
    }
}
