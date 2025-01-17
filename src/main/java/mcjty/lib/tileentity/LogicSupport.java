package mcjty.lib.tileentity;

import mcjty.lib.varia.LogicFacing;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static mcjty.lib.blocks.LogicSlabBlock.LOGIC_FACING;

public class LogicSupport {

    private int powerOutput = 0;

    public static LogicFacing getFacing(BlockState state) {
        return state.getValue(LOGIC_FACING);
    }

    public void setPowerOutput(int powerOutput) {
        this.powerOutput = powerOutput;
    }

    public int getPowerOutput() {
        return powerOutput;
    }

    public void setRedstoneState(GenericTileEntity te, int newout) {
        if (powerOutput == newout) {
            return;
        }
        powerOutput = newout;
        te.setChanged();
        BlockState state = te.getBlockState();
        Direction outputSide = getFacing(state).getInputSide().getOpposite();
        te.getLevel().neighborChanged(te.getBlockPos().relative(outputSide), state.getBlock(), te.getBlockPos());
        //        getWorld().notifyNeighborsOfStateChange(this.pos, this.getBlockType());
    }

    public void checkRedstone(GenericTileEntity te, Level world, BlockPos pos) {
        Direction inputSide = getFacing(world.getBlockState(pos)).getInputSide();
        int power = getInputStrength(world, pos, inputSide);
        te.setPowerInput(power);
    }

    /**
     * Returns the signal strength at one input of the block
     */
    public int getInputStrength(Level world, BlockPos pos, Direction side) {
        int power = world.getSignal(pos.relative(side), side);
        if (power < 15) {
            // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
            // not detected with world.getRedstonePower().
            // Not exactly pretty, but it's how vanilla redstone repeaters do it.
            BlockState blockState = world.getBlockState(pos.relative(side));
            Block b = blockState.getBlock();
            if (b == Blocks.REDSTONE_WIRE) {
                power = Math.max(power, blockState.getValue(RedStoneWireBlock.POWER));
            }
        }

        return power;
    }

    public int getRedstoneOutput(BlockState state, Direction side) {
        if (side == getFacing(state).getInputSide()) {
            return getPowerOutput();
        } else {
            return 0;
        }
    }
}
