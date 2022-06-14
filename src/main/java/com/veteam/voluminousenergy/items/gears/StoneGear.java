package com.veteam.voluminousenergy.items.gears;

import com.veteam.voluminousenergy.items.VEItem;
import com.veteam.voluminousenergy.setup.VESetup;
import net.minecraft.world.item.Item;

public class StoneGear extends VEItem {
    public StoneGear(){
        super(new Item.Properties()
        .stacksTo(64)
        .tab(VESetup.itemGroup)
        );
        setRegistryName("stonegear");
    }
}
