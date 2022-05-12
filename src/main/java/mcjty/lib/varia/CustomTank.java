package mcjty.lib.varia;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public class CustomTank extends FluidTank {

    @Nonnull
    protected FluidStack fluid = FluidStack.EMPTY;

    public CustomTank(long capacity) {
        super(capacity);
    }

    public CustomTank setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    @Nonnull
    public FluidStack getFluid() {
        return fluid;
    }

    @Override
    public long getFluidAmount() {
        return fluid.getAmount();
    }

    @Override
    public CustomTank readFromNBT(CompoundTag nbt) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
        setFluid(fluid);
        return this;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        fluid.writeToNBT(nbt);
        return nbt;
    }

    protected void onContentsChanged() {

    }

    public void setFluid(@Nonnull FluidStack stack) {
        this.fluid = stack;
    }

    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    public long getSpace() {
        return Math.max(0, capacity - fluid.getAmount());
    }

}