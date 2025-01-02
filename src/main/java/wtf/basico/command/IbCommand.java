package wtf.basico.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IbCommand extends Command {

    public IbCommand(String name) {
        super(name);
        this.description = "Toggle breaking blocks instantly";
        this.usageMessage = "/ib";
        this.setPermission("nsmb.command.ib");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Sender must be a player.");
            return true;
        }

        Player p = (Player) sender;
        p.setInstantBreak(!p.canBreakInstantly());
        broadcastCommandMessage(sender, (p.canBreakInstantly() ? "Now" : "No longer") + " mining blocks instantly");
        return true;
    }
}