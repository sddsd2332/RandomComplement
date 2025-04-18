package com.circulation.random_complement.client.buttonsetting;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum InscriberBlockMode implements InterfaceButton {
    OPEN,
    CLOSE;

    @Override
    @SideOnly(Side.CLIENT)
    public String getName(){
        return I18n.format("gui.inscriber.block_mode.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTooltip(){
        return I18n.format("gui.inscriber.block_mode." + this.name() + ".text");
    }
}
