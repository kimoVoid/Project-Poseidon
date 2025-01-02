package wtf.basico.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ToggledownfallCommand extends Command {

    public ToggledownfallCommand(String name) {
        super(name);
        this.description = "Toggle weather";
        this.usageMessage = "/toggledownfall";
        this.setPermission("nsmb.command.toggledownfall");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        broadcastCommandMessage(sender, "Toggled downfall");
        Bukkit.getServer().getWorlds().get(0).setWeatherDuration(1);
        Bukkit.getServer().getWorlds().get(0).setThunderDuration(1);
        return true;
    }
}