package net.lordofthecraft.omniscience.command.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.lordofthecraft.omniscience.OmniConfig;
import net.lordofthecraft.omniscience.Omniscience;
import net.lordofthecraft.omniscience.api.interfaces.IOmniscience;
import net.lordofthecraft.omniscience.command.result.CommandResult;
import net.lordofthecraft.omniscience.command.result.UseResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ToolCommand extends SimpleCommand {

    public ToolCommand() {
        super(ImmutableList.of("t", "inspect"));
    }

    @Override
    public UseResult canRun(CommandSender sender) {
        return sender instanceof Player ? hasPermission(sender, "omniscience.commands.tool") : UseResult.NO_COMMAND_SENDER;
    }

    @Override
    public String getCommand() {
        return "tool";
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Turn on or off the Omniscience search tool";
    }

    @Override
    public CommandResult run(CommandSender sender, IOmniscience core, String[] args) {
        if (!(sender instanceof Player)) {
            return CommandResult.success();
        }

        Player pl = (Player) sender;
        var material = OmniConfig.INSTANCE.getWandMaterial();

        if (Omniscience.hasActiveWand(pl)) {
            int slot = pl.getInventory().first(material);
            boolean inHand = pl.getInventory().getItemInMainHand().getType() == material;

            if (slot == -1) {
                pl.getInventory().addItem(new ItemStack(material));
                pl.sendMessage(GREEN + "Added the Omniscience data tool to your inventory.");
            } else if (!inHand) {
                // magnetic: swap tool into main hand
                ItemStack current = pl.getInventory().getItemInMainHand();
                pl.getInventory().setItemInMainHand(pl.getInventory().getItem(slot));
                pl.getInventory().setItem(slot, current);
                pl.sendMessage(GREEN + "Moved the data tool to your hand.");
            } else {
                Omniscience.wandDeactivateFor(pl);
                pl.sendMessage(GREEN + "Deactivated the Omniscience Data Tool");
            }
        } else {
            Omniscience.wandActivateFor(pl);
            int slot = pl.getInventory().first(material);
            if (slot == -1) {
                pl.getInventory().addItem(new ItemStack(material));
                pl.sendMessage(GREEN + "Added the Omniscience data tool to your inventory.");
            } else {
                boolean inHand = pl.getInventory().getItemInMainHand().getType() == material;
                if (!inHand) {
                    ItemStack current = pl.getInventory().getItemInMainHand();
                    pl.getInventory().setItemInMainHand(pl.getInventory().getItem(slot));
                    pl.getInventory().setItem(slot, current);
                }
                pl.sendMessage(GREEN + "Activated the Omniscience Data Tool " + GRAY + "(" + material.name() + ")");
            }
        }
        return CommandResult.success();
    }

    @Override
    public void buildLiteralArgumentBuilder(LiteralArgumentBuilder<Object> builder) {
        // NO:OP
    }

    @Override
    public List<String> getCommandSuggestions(String partial) {
        return null;
    }
}
