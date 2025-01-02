package org.bukkit.command.defaults;

import net.minecraft.server.Block;
import net.minecraft.server.Item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand extends VanillaCommand {
    public GiveCommand() {
        super("give");
        this.description = "Gives the specified player a certain amount of items";
        this.usageMessage = "/give <player> <item> [amount] [data]";
        this.setPermission("bukkit.command.give");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length < 2 || args.length > 5) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage());
            return false;
        }

        Player player = Bukkit.getPlayerExact(args[0]);
        if (player != null) {
            try {
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    id = nameToItemId(args[1]);
                }

                if (id == -1) {
                    sender.sendMessage(ChatColor.RED + "Item not found.");
                    return false;
                }

                int amount = 1;
                if (args.length > 2) {
                    amount = parseInt(args[2], 1);
                }

                if (amount < 1) {
                    amount = 1;
                }

                if (amount > 64) {
                    amount = 64;
                }

                int data = 0;
                if (args.length > 3) {
                    data = parseInt(args[3], 0);
                }

                if (data < 0) {
                    data = 0;
                }

                broadcastCommandMessage(sender, String.format("Giving %s %s x %s",
                        player.getName(), amount, Item.byId[id].j() + (data > 0 ? ":" + data : "")));
                player.getInventory().addItem(new ItemStack(id, amount, (short)data));
            } catch (NumberFormatException var11) {
                sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Can't find player \"" + args[0] + "\"");
        }

        return true;
    }

    @Override
    public boolean matches(String input) {
        return input.startsWith("give ");
    }

    public static int nameToItemId(String n) {
        String name = n.replace("_", "");

        /* Attempt to find item name */
        for (int i = 0; i < Item.byId.length; i++) {
            if (Item.byId[i] == null)
                continue;
            String translatedName = Item.byId[i].j().replace(" ", ""); // Remove spaces
            if (translatedName.equalsIgnoreCase(name)) {
                return i;
            }
        }

        /* Attempt to find block name */
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

    private int parseInt(String s, int min) throws NumberFormatException {
        return Math.max(Integer.parseInt(s), min);
    }
}