package wtf.basico.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ClearCommand extends Command {

    public ClearCommand(String name) {
        super(name);
        this.description = "Clear inventory";
        this.usageMessage = "/clear [player]";
        this.setPermission("nsmb.command.clear");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        if (sender instanceof ConsoleCommandSender && args.length < 1) {
            sender.sendMessage("§cUsage: " + this.getUsage());
            return false;
        }
        String target = args.length > 0 ? args[0] : sender.getName();
        Player p = Bukkit.getServer().getPlayer(target);
        if (p == null) {
            sender.sendMessage(ChatColor.RED + "Can't find player \"" + args[0] + "\"");
            return false;
        }

        int items = 0;

        p.getInventory().clear();
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);

        broadcastCommandMessage(sender, String.format("Cleared %s item(s) from %s inventory", items, sender.getName().equals(p.getName()) ? "your" : p.getName() + "'s"));
        return true;
    }
}