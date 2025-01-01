package net.ornithemc.osl.networking.impl;

import net.minecraft.server.*;
import net.ornithemc.osl.networking.api.Channels;
import net.ornithemc.osl.networking.api.CustomPayload;
import net.ornithemc.osl.networking.api.DataStreams;
import net.ornithemc.osl.networking.api.IOConsumer;
import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class NetServerHandlerImpl {

    private static final Logger LOGGER = Logger.getLogger("OSL Networking");

    private static MinecraftServer server;

    public static void setUp(MinecraftServer server) {
        if (NetServerHandlerImpl.server == server) {
            throw new IllegalStateException("tried to set up server networking when it was already set up!");
        }

        NetServerHandlerImpl.server = server;
    }

    public static void destroy(MinecraftServer server) {
        if (NetServerHandlerImpl.server != server) {
            throw new IllegalStateException("tried to destroy server networking when it was not set up!");
        }

        NetServerHandlerImpl.server = null;
    }

    public static final Map<String, Listener> LISTENERS = new LinkedHashMap<>();

    public static <T extends CustomPayload> void registerListener(String channel, Supplier<T> initializer, ServerPlayNetworking.PayloadListener<T> listener) {
        registerListenerImpl(channel, (server, handler, player, data) -> {
            T payload = initializer.get();
            payload.read(DataStreams.input(data));

            return listener.handle(server, handler, player, payload);
        });
    }

    public static void registerListener(String channel, ServerPlayNetworking.StreamListener listener) {
        registerListenerImpl(channel, (server, handler, player, data) -> {
            return listener.handle(server, handler, player, DataStreams.input(data));
        });
    }

    public static void registerListenerRaw(String channel, ServerPlayNetworking.ByteArrayListener listener) {
        registerListenerImpl(channel, listener::handle);
    }

    private static void registerListenerImpl(String channel, Listener listener) {
        LISTENERS.compute(channel, (key, value) -> {
            Channels.validate(channel);

            if (value != null) {
                throw new IllegalStateException("there is already a listener on channel \'" + channel + "\'");
            }

            return listener;
        });
    }

    public static void unregisterListener(String channel) {
        LISTENERS.remove(channel);
    }

    public static boolean handle(NetServerHandler handler, Packet250CustomPayload packet) {
        Listener listener = LISTENERS.get(packet.channel);

        if (listener != null) {
            EntityPlayer player = handler.player;

            try {
                return listener.handle(server, handler, player, packet.data);
            } catch (IOException e) {
                LOGGER.warning("error handling custom payload on channel '" + packet.channel + "': " + e);
                return true;
            }
        }

        return false;
    }

    public static boolean isPlayReady(EntityPlayer player) {
        return player.netServerHandler != null && player.netServerHandler.isPlayReady();
    }

    public static boolean canSend(EntityPlayer player, String channel) {
        return player.netServerHandler != null && player.netServerHandler.isRegisteredClientChannel(channel);
    }

    public static void send(EntityPlayer player, String channel, CustomPayload payload) {
        if (canSend(player, channel)) {
            doSend(player, channel, payload);
        }
    }

    public static void send(EntityPlayer player, String channel, IOConsumer<DataOutputStream> writer) {
        if (canSend(player, channel)) {
            doSend(player, channel, writer);
        }
    }

    public static void send(EntityPlayer player, String channel, byte[] data) {
        if (canSend(player, channel)) {
            doSend(player, channel, data);
        }
    }

    public static void send(Iterable<EntityPlayer> players, String channel, CustomPayload payload) {
        sendPacket(collectPlayers(players, p -> canSend(p, channel)), makePacket(channel, payload));
    }

    public static void send(Iterable<EntityPlayer> players, String channel, IOConsumer<DataOutputStream> writer) {
        sendPacket(collectPlayers(players, p -> canSend(p, channel)), makePacket(channel, writer));
    }

    public static void send(Iterable<EntityPlayer> players, String channel, byte[] data) {
        sendPacket(collectPlayers(players, p -> canSend(p, channel)), makePacket(channel, data));
    }

    public static void send(int dimension, String channel, CustomPayload payload) {
        doSend(collectPlayers(p -> p.dimension == dimension && canSend(p, channel)), channel, payload);
    }

    public static void send(int dimension, String channel, IOConsumer<DataOutputStream> writer) {
        doSend(collectPlayers(p -> p.dimension == dimension && canSend(p, channel)), channel, writer);
    }

    public static void send(int dimension, String channel, byte[] data) {
        doSend(collectPlayers(p -> p.dimension == dimension && canSend(p, channel)),channel, data);
    }

    public static void send(String channel, CustomPayload payload) {
        doSend(collectPlayers(p -> canSend(p, channel)), channel, payload);
    }

    public static void send(String channel, IOConsumer<DataOutputStream> writer) {
        doSend(collectPlayers(p -> canSend(p, channel)), channel, writer);
    }

    public static void send(String channel, byte[] data) {
        doSend(collectPlayers(p -> canSend(p, channel)), channel, data);
    }

    public static void doSend(EntityPlayer player, String channel, CustomPayload payload) {
        sendPacket(player, makePacket(channel, payload));
    }

    public static void doSend(EntityPlayer player, String channel, IOConsumer<DataOutputStream> writer) {
        sendPacket(player, makePacket(channel, writer));
    }

    public static void doSend(EntityPlayer player, String channel, byte[] data) {
        sendPacket(player, makePacket(channel, data));
    }

    public static void doSend(Iterable<EntityPlayer> players, String channel, CustomPayload payload) {
        sendPacket(players, makePacket(channel, payload));
    }

    public static void doSend(Iterable<EntityPlayer> players, String channel, IOConsumer<DataOutputStream> writer) {
        sendPacket(players, makePacket(channel, writer));
    }

    public static void doSend(Iterable<EntityPlayer> players, String channel, byte[] data) {
        sendPacket(players, makePacket(channel, data));
    }

    public static void doSend(int dimension, String channel, CustomPayload payload) {
        doSend(collectPlayers(p -> p.dimension == dimension), channel, payload);
    }

    public static void doSend(int dimension, String channel, IOConsumer<DataOutputStream> writer) {
        doSend(collectPlayers(p -> p.dimension == dimension), channel, writer);
    }

    public static void doSend(int dimension, String channel, byte[] data) {
        doSend(collectPlayers(p -> p.dimension == dimension),channel, data);
    }

    public static void doSend(String channel, CustomPayload payload) {
        doSend(collectPlayers(p -> true), channel, payload);
    }

    public static void doSend(String channel, IOConsumer<DataOutputStream> writer) {
        doSend(collectPlayers(p -> true), channel, writer);
    }

    public static void doSend(String channel, byte[] data) {
        doSend(collectPlayers(p -> true), channel, data);
    }

    @SuppressWarnings("unchecked") // thanks proguard
    private static Iterable<EntityPlayer> collectPlayers(Predicate<EntityPlayer> filter) {
        return collectPlayers(server.serverConfigurationManager.players, filter);
    }

    private static Iterable<EntityPlayer> collectPlayers(Iterable<EntityPlayer> src, Predicate<EntityPlayer> filter) {
        List<EntityPlayer> players = new ArrayList<>();

        for (EntityPlayer player : src) {
            if (filter.test(player)) {
                players.add(player);
            }
        }

        return players;
    }

    private static Packet makePacket(String channel, CustomPayload payload) {
        return makePacket(channel, payload::write);
    }

    private static Packet makePacket(String channel, IOConsumer<DataOutputStream> writer) {
        try {
            return new Packet250CustomPayload(channel, DataStreams.output(writer).toByteArray());
        } catch (IOException e) {
            LOGGER.warning("error writing custom payload to channel '" + channel + "': " + e);
            return null;
        }
    }

    private static Packet makePacket(String channel, byte[] data) {
        return new Packet250CustomPayload(channel, data);
    }

    private static void sendPacket(EntityPlayer player, Packet packet) {
        if (packet != null) {
            player.netServerHandler.sendPacket(packet);
        }
    }

    private static void sendPacket(Iterable<EntityPlayer> players, Packet packet) {
        if (packet != null) {
            for (EntityPlayer player : players) {
                sendPacket(player, packet);
            }
        }
    }

    private interface Listener {

        boolean handle(MinecraftServer server, NetServerHandler handler, EntityPlayer player, byte[] data) throws IOException;

    }
}