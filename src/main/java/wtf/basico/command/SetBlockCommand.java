package wtf.basico.command;

import net.minecraft.server.Block;
import net.minecraft.server.MathHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

public class SetBlockCommand extends NSMBCommand {

    public SetBlockCommand(String name) {
        super(name);
        this.description = "Set a block in specific coordinates";
        this.usageMessage = "/setblock <x> <y> <z> <id> [data]";
        this.setPermission("nsmb.command.setblock");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Sender must be a player.");
            return true;
        }

        Player p = (Player) sender;

        if (args.length >= 4) {
            int x;
            int y;
            int z;
            try {
                x = MathHelper.floor(parseCoordinate(p.getLocation().getX(), args[0]));
                y = MathHelper.floor(parseCoordinate(p.getLocation().getY(), args[1]));
                z = MathHelper.floor(parseCoordinate(p.getLocation().getZ(), args[2]));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Usage: " + getUsage() + ". Invalid coordinates.");
                return true;
            }

            int blockId;
            try {
                blockId = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                blockId = nameToBlockId(args[3]);
            }

            if (blockId == -1) {
                sender.sendMessage(ChatColor.RED + "Block not found.");
                return true;
            }

            int data = 0;
            if (args.length >= 5) {
                try {
                    data = parseInt(args[4], 0, 15);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Usage: " + getUsage() + ". Invalid data.");
                    return true;
                }
            }

            if (!((CraftWorld)p.getWorld()).getHandle().isChunkLoaded(x, y, z)) {
                sender.sendMessage(ChatColor.RED + String.format("Chunk at position [%s, %s, %s] is not loaded.", x, y, z));
                return true;
            }

            if (!p.getWorld().getBlockAt(x, y, z).setTypeIdAndData(blockId, (byte)data, false)) {
                sender.sendMessage(ChatColor.RED + String.format("Block [%s, %s, %s] was not changed.", x, y, z));
            } else {
                broadcastCommandMessage(sender, String.format("Set block at position [%s, %s, %s] to %s%s",
                        x, y, z, Block.byId[blockId].k(), data != 0 ? ":" + data : ""));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
        }
        return true;
    }

    public static int nameToBlockId(String n) {
        String name = n.replace("_", "");
        for (int i = 0; i < Block.byId.length; i++) {
            if (Block.byId[i] == null)
                continue;
            String translatedName = Block.byId[i].k().replace(" ", ""); // Remove spaces
            if (translatedName.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }
}