package com.pau101.auginter.common;

import java.io.File;

import com.pau101.auginter.AugmentedInteractions;
import com.pau101.auginter.server.net.MessageAdapter;
import com.pau101.auginter.server.net.MessageSetAnimationVisibility;

import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public abstract class Proxy implements IMessageHandler<MessageAdapter, IMessage> {
	private SimpleNetworkWrapper network;

	private int nextMessageId;

	public void init(File configFile) {
		network = NetworkRegistry.INSTANCE.newSimpleChannel(AugmentedInteractions.ID);
		registerMessage(MessageSetAnimationVisibility.class, Side.CLIENT);
	}

	public final SimpleNetworkWrapper getNetwork() {
		return network;
	}

	public abstract Configurator getConfigurator();

	public abstract boolean rightClickMouse(EnumHand hand);

	public void setAllAnimationVisiblity(boolean value) {}

	@Override
	public final IMessage onMessage(MessageAdapter msg, MessageContext ctx) {
		IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
		thread.addScheduledTask(() -> msg.process(ctx));
		return null;
	}

	private void registerMessage(Class<? extends MessageAdapter> clazz, Side toSide) {
		network.registerMessage(this, clazz, nextMessageId++, toSide);
	}
}
