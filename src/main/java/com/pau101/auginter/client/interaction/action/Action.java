package com.pau101.auginter.client.interaction.action;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public interface Action<D> {
	boolean perform(Minecraft mc, D dat);
}
