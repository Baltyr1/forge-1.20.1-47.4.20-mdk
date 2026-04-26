package net.baltyr.opmmod.classes;

import net.minecraft.nbt.CompoundTag;

public class PlayerClassCapability {

    private OpmClass playerClass = OpmClass.NONE;

    public OpmClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(OpmClass c) {
        this.playerClass = c;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("playerClass", playerClass.name());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        playerClass = OpmClass.fromString(tag.getString("playerClass"));
    }
}