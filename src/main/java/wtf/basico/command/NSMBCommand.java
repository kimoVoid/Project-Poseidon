package wtf.basico.command;

import org.bukkit.command.Command;

public abstract class NSMBCommand extends Command {

    public NSMBCommand(String name) {
        super(name);
    }

    public static int parseInt(String s, int min) {
        return parseInt(s, min, 2147483647);
    }

    public static int parseInt(String s, int min, int max) throws NumberFormatException {
        int var4 = Integer.parseInt(s);
        if (var4 < min) {
            throw new NumberFormatException();
        } else if (var4 > max) {
            throw new NumberFormatException();
        } else {
            return var4;
        }
    }

    public static double parseDouble(String s) throws NumberFormatException {
        try {
            double var2 = Double.parseDouble(s);
            if (!Double.isFinite(var2)) {
                throw new NumberFormatException();
            } else {
                return var2;
            }
        } catch (NumberFormatException var4) {
            throw new NumberFormatException();
        }
    }

    public static double parseCoordinate(double c, String s) {
        return parseCoordinate(c, s, -30000000, 30000000);
    }

    public static double parseCoordinate(double c, String s, int min, int max) throws NumberFormatException {
        boolean var6 = s.startsWith("~");
        if (var6 && Double.isNaN(c)) {
            throw new NumberFormatException();
        } else {
            double var7 = var6 ? c : 0.0D;
            if (!var6 || s.length() > 1) {
                boolean var9 = s.contains(".");
                if (var6) {
                    s = s.substring(1);
                }

                var7 += parseDouble(s);
                if (!var9 && !var6) {
                    var7 += 0.5D;
                }
            }

            if (min != 0 || max != 0) {
                if (var7 < (double)min) {
                    throw new NumberFormatException();
                }

                if (var7 > (double)max) {
                    throw new NumberFormatException();
                }
            }

            return var7;
        }
    }
}