package org.infinite.empires;

import net.minecraft.world.item.component.CustomModelData;

public enum CustomModelDatas {
    BATTERY,
    GENERATOR,
    /*emit:2*/;
    
    CustomModelDatas() {}
    
    public CustomModelData get() {
        return get((byte) 0);
    }
    
    public CustomModelData get(byte additionalData) {
        return new CustomModelData((0x69650000 + this.ordinal()) | (((short)additionalData) << 1));
    }
}
