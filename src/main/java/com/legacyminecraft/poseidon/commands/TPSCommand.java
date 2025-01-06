package com.legacyminecraft.poseidon.commands;

import com.legacyminecraft.poseidon.Poseidon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TPSCommand extends Command {

    private final LinkedHashMap<String, Integer> intervals = new LinkedHashMap<>();

    public TPSCommand(String name) {
        super(name);
        this.description = "Shows the server's TPS for various intervals";
        this.usageMessage = "/tps";
        this.setPermission("poseidon.command.tps");

        // Define the intervals for TPS calculation
        intervals.put("1m", 60);
        intervals.put("5m", 300);
        intervals.put("10m", 600);
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        LinkedList<Double> tpsRecords = Poseidon.getTpsRecords();
        double mspt = this.round(2, this.average(Poseidon.getAverageTickTimes()) * 1.0E-6D);

        sender.sendMessage(String.format("§7Server TPS: %s §7MSPT: %s", formatTps(calculateAverage(tpsRecords, 0)), formatMspt(mspt)));
        StringBuilder message = new StringBuilder("§7TPS History: ");

        // Calculate and format TPS for each interval dynamically
        for (Map.Entry<String, Integer> entry : intervals.entrySet()) {
            double averageTps = calculateAverage(tpsRecords, entry.getValue());
            message.append(formatTps(averageTps)).append(" (").append(entry.getKey()).append(")§7, ");
        }

        // Remove the trailing comma and space
        if (message.length() > 0) {
            message.setLength(message.length() - 2);
        }

        // Get memory info
        Runtime runtime = Runtime.getRuntime();

        int mb = 1048576;
        String used = "  §7Used: " + (runtime.totalMemory() - runtime.freeMemory()) / (long)mb + " MB / " + runtime.totalMemory() / (long)mb + " MB";
        String free = "  §7Free: " + runtime.freeMemory() / (long)mb + " MB";
        String max = "  §7Max: " + runtime.maxMemory() / (long)mb + " MB";

        sender.sendMessage(message.toString());
        sender.sendMessage("§7Memory:");
        sender.sendMessage(used);
        sender.sendMessage(free);
        sender.sendMessage(max);
        return true;
    }

    private double calculateAverage(LinkedList<Double> records, int seconds) {
        int size = Math.min(records.size(), seconds);
        if (size == 0) return 20.0;

        double total = 0;
        for (int i = 0; i < size; i++) {
            total += records.get(i);
        }
        return total / size;
    }

    private String formatTps(double tps) {
        String colorCode;
        if (tps >= 19) {
            colorCode = "§a";
        } else if (tps >= 15) {
            colorCode = "§e";
        } else {
            colorCode = "§c";
        }
        return colorCode + String.format("%.2f", tps);
    }

    private String formatMspt(double mspt) {
        String color = (mspt < 40) ? "§a" : (mspt < 45) ? "§e" : (mspt < 50) ? "§6" : "§c";
        return String.format("%s%.2f", color, mspt);
    }

    public double round(int places, double value) {
        return (new BigDecimal(value)).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }

    public double average(long[] times) {
        long avg = 0L;
        for (long time : times) {
            avg += time;
        }
        return (double)avg / (double)times.length;
    }
}
