package com.pau101.auginter;

import com.pau101.auginter.common.Configurator;
import com.pau101.auginter.common.Proxy;
import com.pau101.auginter.server.command.CommandAugInter;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
	modid = AugmentedInteractions.ID,
	name = AugmentedInteractions.NAME,
	version = AugmentedInteractions.VERSION,
	guiFactory = "com.pau101.auginter.client.gui.AugInterGuiFactory"
)
public final class AugmentedInteractions {
	public static final String ID = "auginter";

	public static final String NAME = "Augmented Interactions";

	public static final String VERSION = "1.0.0";

	@SidedProxy(
		clientSide = "com.pau101.auginter.client.ClientProxy",
		serverSide = "com.pau101.auginter.server.ServerProxy"
	)
	private static Proxy proxy;

	@EventHandler
	public void init(FMLPreInitializationEvent event) {
		proxy.init(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandAugInter());
	}

	public static Configurator getConfigurator() {
		return proxy.getConfigurator();
	}

	public static SimpleNetworkWrapper getNetwork() {
		return proxy.getNetwork();
	}

	public static void setAllAnimationVisibility(boolean value) {
		proxy.setAllAnimationVisiblity(value);
	}
}
