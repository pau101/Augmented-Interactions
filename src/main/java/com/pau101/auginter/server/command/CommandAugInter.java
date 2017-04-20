package com.pau101.auginter.server.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.pau101.auginter.AugmentedInteractions;
import com.pau101.auginter.server.net.MessageSetAnimationVisibility;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public final class CommandAugInter extends CommandBase {
	private static final String ANIMATION = "animation";

	@Override
	public String getName() {
		return "auginter";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "command.usage.auginter";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			throw new WrongUsageException(getUsage(sender));
		}
		String option = args[0];
		if (ANIMATION.equalsIgnoreCase(option)) {
			if (args.length < 3) {
				throw new WrongUsageException("command.usage.auginter.animation");
			}
			EntityPlayerMP player = getPlayer(server, sender, args[1]);
			boolean enable = parseBoolean(args[2]);
			AugmentedInteractions.getNetwork().sendTo(new MessageSetAnimationVisibility(enable), player);
            notifyCommandListener(sender, this, enable ? "commands.auginter.animation.enable" : "commands.auginter.animation.disable", player.getName());
		} else {
			throw new WrongUsageException(getUsage(sender));
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		int len = args.length;
		if (len == 1) {
			return Collections.singletonList(ANIMATION);
		} else if (len > 1) {
			String option = args[0];
			if (ANIMATION.equals(option)) {
				if (len == 2) {
					return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				} else if (len == 3) {
					return Arrays.asList("true", "false");
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 1 && ANIMATION.equals(args[0]);
	}
}
