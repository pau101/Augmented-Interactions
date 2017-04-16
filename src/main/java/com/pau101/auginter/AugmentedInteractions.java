package com.pau101.auginter;

import com.pau101.auginter.server.ServerProxy;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
	modid = AugmentedInteractions.ID,
	name = AugmentedInteractions.NAME,
	version = AugmentedInteractions.VERSION
)
public class AugmentedInteractions {
	public static final String ID = "auginter";

	public static final String NAME = "Augmented Interactions";

	public static final String VERSION = "1.0.0";

	@SidedProxy(
		clientSide = "com.pau101.auginter.client.ClientProxy",
		serverSide = "com.pau101.auginter.server.ServerProxy"
	)
	public static ServerProxy proxy;

	@EventHandler
	public void init(FMLPreInitializationEvent event) {
		proxy.init();
	}

	public static boolean rightClickMouse(EnumHand hand) {
		return proxy.rightClickMouse(hand);
	}
}
