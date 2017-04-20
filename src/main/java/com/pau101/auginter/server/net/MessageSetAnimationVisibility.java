package com.pau101.auginter.server.net;

import java.io.IOException;

import com.pau101.auginter.AugmentedInteractions;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageSetAnimationVisibility extends MessageAdapter {
	private boolean state;

	public MessageSetAnimationVisibility() {}

	public MessageSetAnimationVisibility(boolean enable) {
		this.state = enable;
	}

	@Override
	public void serialize(PacketBuffer buf) {
		buf.writeBoolean(state);
	}

	@Override
	public void deserialize(PacketBuffer buf) throws IOException {
		state = buf.readBoolean();
	}

	@Override
	public void process(MessageContext ctx) {
		AugmentedInteractions.setAllAnimationVisibility(state);
	}
}
