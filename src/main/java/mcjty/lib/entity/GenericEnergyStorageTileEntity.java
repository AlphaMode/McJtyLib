package mcjty.lib.entity;

import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketRequestIntegerFromServer;
import mcjty.lib.typed.TypedMap;
import net.minecraft.nbt.NBTTagCompound;

public class GenericEnergyStorageTileEntity extends GenericTileEntity {

    public static final String CMD_GETENERGY = "getEnergy";
    public static final String CLIENTCMD_GETENERGY = "getEnergy";

    protected McJtyEnergyStorage storage;

    private static int currentRF = 0;

    private int requestRfDelay = 3;

    public void modifyEnergyStored(int energy) {
        storage.modifyEnergyStored(energy);
    }

    public GenericEnergyStorageTileEntity(int maxEnergy, int maxReceive) {
        storage = new McJtyEnergyStorage(maxEnergy);
        storage.setMaxReceive(maxReceive);
    }

    public GenericEnergyStorageTileEntity(int maxEnergy, int maxReceive, int maxExtract) {
        storage = new McJtyEnergyStorage(maxEnergy);
        storage.setMaxReceive(maxReceive);
        storage.setMaxExtract(maxExtract);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        storage.readFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        storage.writeToNBT(tagCompound);
    }

    public static int getCurrentRF() {
        return currentRF;
    }

    public static void setCurrentRF(int currentRF) {
        GenericEnergyStorageTileEntity.currentRF = currentRF;
    }

    // Request the RF from the server. This has to be called on the client side.
    public void requestRfFromServer(String modid) {
        requestRfDelay--;
        if (requestRfDelay > 0) {
            return;
        }
        requestRfDelay = 3;
        PacketHandler.modNetworking.get(modid).sendToServer(new PacketRequestIntegerFromServer(modid, pos,
                CMD_GETENERGY,
                CLIENTCMD_GETENERGY, TypedMap.EMPTY));
    }

    @Override
    public Integer executeWithResultInteger(String command, TypedMap args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETENERGY.equals(command)) {
            return storage.getEnergyStored();
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETENERGY.equals(command)) {
            setCurrentRF(result);
            return true;
        }
        return false;
    }
}
