package wtf.basico.command;

import net.minecraft.server.WorldData;
import net.minecraft.server.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;

public class DebugCommand extends Command {

    public DebugCommand(String name) {
        super(name);
        this.description = "Shows information about the world";
        this.usageMessage = "/debug [type]";
        this.setPermission("nsmb.command.debug");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        String types = "weather, seed, spawn, day";

        if (args.length != 1) {
            sender.sendMessage("§cUsage: " + getUsage() + ". Valid types: §r" + types);
            return true;
        }

        String type = args[0].toLowerCase();
        WorldServer world = ((CraftWorld)Bukkit.getWorlds().get(0)).getHandle();
        WorldData data = world.worldData;

        switch (type) {
            case "weather":
                sender.sendMessage("rainTime: " + data.getWeatherDuration() + " ");
                sender.sendMessage("thunderTime: " + data.getThunderDuration());
                break;
            case "seed":
                sender.sendMessage("seed: " + data.getSeed());
                break;
            case "spawn":
                sender.sendMessage("spawn: " + data.c() + ", " + data.d() + ", " + data.e());
                break;
            case "day":
                sender.sendMessage("total time: " + data.f() + String.format(" ticks (day %.2f)", data.f() / 24000f));
                break;
            default:
                sender.sendMessage("§cUsage: " + getUsage() + ". Valid types: §r" + types);
        }
        return true;
    }
}