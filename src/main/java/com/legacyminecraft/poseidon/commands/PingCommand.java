package com.legacyminecraft.poseidon.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand extends Command {

    public PingCommand(String name) {
        super(name);
        this.description = "Shows a player's ping";
        this.usageMessage = "/ping [player]";
        this.setPermission("nsmb.command.ping");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length < 1 && !(sender instanceof Player)) {
            sender.sendMessage(String.format("§cUsage: %s. Sender must be a player.", this.usageMessage));
            return true;
        }

        Player target;

        if (args.length < 1) {
            target = Bukkit.getPlayer(sender.getName());
        } else {
            if (Bukkit.getPlayer(args[0]) == null) {
                sender.sendMessage(String.format("§cUsage: %s. Player %s is not online.", this.usageMessage, args[0]));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
        }

        int ping = target.getPing();
        sender.sendMessage(String.format("§7%s ping is: %s ms", target.getName().equals(sender.getName()) ? "Your" : target.getName() + "'s", ping));
        return true;
    }
}