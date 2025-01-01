package wtf.basico.networking;

import net.minecraft.server.EntityPlayer;
import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import wtf.basico.networking.payload.PlayerInfoPayload;

public class PlayerInfoManager {

    public static final PlayerInfoManager INSTANCE = new PlayerInfoManager();

    public void sendPlayerInfo(String username, boolean online, int ping) {
        ServerPlayNetworking.send("BetaQOL|PlayerInfo", new PlayerInfoPayload(username, online, ping));
    }

    public void sendPlayerInfo(EntityPlayer to) {
        for (Player on : Bukkit.getServer().getOnlinePlayers()) {
            ServerPlayNetworking.send(to, "BetaQOL|PlayerInfo", new PlayerInfoPayload(
                    on.getName(),
                    true,
                    on.getPing()));
        }
    }
}