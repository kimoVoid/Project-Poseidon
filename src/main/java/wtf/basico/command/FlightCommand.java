package wtf.basico.command;

import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import wtf.basico.networking.payload.FlightPayload;

public class FlightCommand extends Command {

    public FlightCommand(String name) {
        super(name);
        this.description = "Toggle flight (requires BHCreative)";
        this.usageMessage = "/fly";
        this.setPermission("nsmb.command.flight");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSender must be a player.");
            return true;
        }

        Player player = (Player) sender;
        player.setFly(!player.canFly());
        ServerPlayNetworking.send(((CraftPlayer)player).getHandle(), "BHCreative|Flight", new FlightPayload(player.canFly()));
        broadcastCommandMessage(sender, "Flight " + (player.canFly() ? "enabled" : "disabled"));
        return true;
    }
}