package wtf.basico.command;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.WorldServer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

public class SummonCommand extends NSMBCommand {

    public SummonCommand(String name) {
        super(name);
        this.description = "Summon an entity";
        this.usageMessage = "/summon <entity> [x y z]";
        this.setPermission("nsmb.command.summon");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Sender must be a player.");
            return true;
        }

        Player p = (Player) sender;

        double x = p.getLocation().getX();
        double y = p.getLocation().getY();
        double z = p.getLocation().getZ();

        if (args.length > 1) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
                return true;
            }
            try {
                x = parseCoordinate(p.getLocation().getX(), args[1]);
                y = parseCoordinate(p.getLocation().getY(), args[2]);
                z = parseCoordinate(p.getLocation().getZ(), args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage() + ". Invalid coordinates.");
                return true;
            }
        }

        try {
            WorldServer world = ((CraftWorld)p.getWorld()).getHandle();
            Entity entity = EntityTypes.a(args[0], world);
            entity.setPosition(x, y, z);
            world.addEntity(entity);
            broadcastCommandMessage(p, "Summoned " + EntityTypes.b(entity));
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage() + ". Invalid entity.");
        }
        return true;
    }
}