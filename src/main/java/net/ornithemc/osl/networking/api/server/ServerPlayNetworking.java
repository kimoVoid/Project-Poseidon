package net.ornithemc.osl.networking.api.server;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.ornithemc.osl.networking.api.CustomPayload;
import net.ornithemc.osl.networking.api.IOConsumer;
import net.ornithemc.osl.networking.impl.NetServerHandlerImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

public final class ServerPlayNetworking {

    /**
     * Register a listener to receive data from the server through the given channel.
     * This listener will only be called from the main thread.
     * A channel can be any String of length 20 or less.
     */
    public static <T extends CustomPayload> void registerListener(String channel, Supplier<T> initializer, PayloadListener<T> listener) {
        NetServerHandlerImpl.registerListener(channel, initializer, listener);
    }

    /**
     * Register a listener to receive data from the server through the given channel.
     * This listener will only be called from the main thread.
     * A channel can be any String of length 20 or less.
     */
    public static void registerListener(String channel, StreamListener listener) {
        NetServerHandlerImpl.registerListener(channel, listener);
    }

    /**
     * Register a listener to receive data from the server through the given channel.
     * This listener will only be called from the main thread.
     * A channel can be any String of length 20 or less.
     */
    public static void registerListenerRaw(String channel, ByteArrayListener listener) {
        NetServerHandlerImpl.registerListenerRaw(channel, listener);
    }

    /**
     * Remove the listener registered to the given channel.
     */
    public static void unregisterListener(String channel) {
        NetServerHandlerImpl.unregisterListener(channel);
    }

    /**
     * Check whether the connection is ready for data to be sent to the client.
     */
    public static boolean isPlayReady(EntityPlayer player) {
        return NetServerHandlerImpl.isPlayReady(player);
    }

    /**
     * Check whether the given channel is open for data to be sent through it.
     * This method will return {@code false} if the client has no listeners for
     * the given channel.
     */
    public static boolean canSend(EntityPlayer player, String channel) {
        return NetServerHandlerImpl.canSend(player, channel);
    }

    /**
     * Send a packet to the given player through the given channel. The payload
     * will only be written if the channel is open.
     */
    public static void send(EntityPlayer player, String channel, CustomPayload payload) {
        NetServerHandlerImpl.send(player, channel, payload);
    }

    /**
     * Send a packet to the given player through the given channel. The writer
     * will only be called if the channel is open.
     */
    public static void send(EntityPlayer player, String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.send(player, channel, writer);
    }

    /**
     * Send a packet to the given player through the given channel.
     */
    public static void send(EntityPlayer player, String channel, byte[] data) {
        NetServerHandlerImpl.send(player, channel, data);
    }

    /**
     * Send a packet to the given players through the given channel. The payload
     * will only be written if the channel is open for at least one player.
     */
    public static void send(Iterable<EntityPlayer> players, String channel, CustomPayload payload) {
        NetServerHandlerImpl.send(players, channel, payload);
    }

    /**
     * Send a packet to the given players through the given channel. The writer
     * will only be called if the channel is open for at least one player.
     */
    public static void send(Iterable<EntityPlayer> players, String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.send(players, channel, writer);
    }

    /**
     * Send a packet to the given players through the given channel.
     */
    public static void send(Iterable<EntityPlayer> players, String channel, byte[] data) {
        NetServerHandlerImpl.send(players, channel, data);
    }

    /**
     * Send a packet to the players in the given dimension through the given
     * channel. The payload will only be written if the channel is open for at
     * least one player.
     */
    public static void send(int dimension, String channel, CustomPayload payload) {
        NetServerHandlerImpl.send(dimension, channel, payload);
    }

    /**
     * Send a packet to the players in the given dimension through the given
     * channel. The writer will only be called if the channel is open for at
     * least one player.
     */
    public static void send(int dimension, String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.send(dimension, channel, writer);
    }

    /**
     * Send a packet to the players in the given dimension through the given
     * channel.
     */
    public static void send(int dimension, String channel, byte[] data) {
        NetServerHandlerImpl.send(dimension, channel, data);
    }

    /**
     * Send a packet to all players through the given channel. The payload will
     * only be written if the channel is open for at least one player.
     */
    public static void send(String channel, CustomPayload payload) {
        NetServerHandlerImpl.send(channel, payload);
    }

    /**
     * Send a packet to all players through the given channel. The writer will
     * only be called if the channel is open for at least one player.
     */
    public static void send(String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.send(channel, writer);
    }

    /**
     * Send a packet to all players through the given channel.
     */
    public static void send(String channel, byte[] data) {
        NetServerHandlerImpl.send(channel, data);
    }

    /**
     * Send a packet to the given player through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(EntityPlayer player, String channel, CustomPayload payload) {
        NetServerHandlerImpl.doSend(player, channel, payload);
    }

    /**
     * Send a packet to the given player through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(EntityPlayer player, String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.doSend(player, channel, writer);
    }

    /**
     * Send a packet to the given player through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(EntityPlayer player, String channel, byte[] data) {
        NetServerHandlerImpl.doSend(player, channel, data);
    }

    /**
     * Send a packet to the given players through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(Iterable<EntityPlayer> players, String channel, CustomPayload payload) {
        NetServerHandlerImpl.doSend(players, channel, payload);
    }

    /**
     * Send a packet to the given players through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(Iterable<EntityPlayer> players, String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.doSend(players, channel, writer);
    }

    /**
     * Send a packet to the given players through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(Iterable<EntityPlayer> players, String channel, byte[] data) {
        NetServerHandlerImpl.doSend(players, channel, data);
    }

    /**
     * Send a packet to the players in the given dimension through the given
     * channel, without checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(int dimension, String channel, CustomPayload payload) {
        NetServerHandlerImpl.doSend(dimension, channel, payload);
    }

    /**
     * Send a packet to the players in the given dimension through the given
     * channel, without checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(int dimension, String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.doSend(dimension, channel, writer);
    }

    /**
     * Send a packet to the players in the given dimension through the given
     * channel, without checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(int dimension, String channel, byte[] data) {
        NetServerHandlerImpl.doSend(dimension, channel, data);
    }

    /**
     * Send a packet to all players through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(String channel, CustomPayload payload) {
        NetServerHandlerImpl.doSend(channel, payload);
    }

    /**
     * Send a packet to all players through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(String channel, IOConsumer<DataOutputStream> writer) {
        NetServerHandlerImpl.doSend(channel, writer);
    }

    /**
     * Send a packet to all players through the given channel, without
     * checking whether it is open.
     * USE WITH CAUTION. Careless use of this method could lead to packet and log
     * spam on the client.
     */
    public static void doSend(String channel, byte[] data) {
        NetServerHandlerImpl.doSend(channel, data);
    }

    public interface PayloadListener<T extends CustomPayload> {

        /**
         * Receive incoming data from the client.
         *
         * @return
         *  Whether the data is consumed. Should only return {@code false} if the
         *  data is completely ignored.
         */
        boolean handle(MinecraftServer server, NetServerHandler handler, EntityPlayer player, T payload) throws IOException;

    }

    public interface StreamListener {

        /**
         * Receive incoming data from the client.
         *
         * @return
         *  Whether the data is consumed. Should only return {@code false} if the
         *  data is completely ignored.
         */
        boolean handle(MinecraftServer server, NetServerHandler handler, EntityPlayer player, DataInputStream data) throws IOException;

    }

    public interface ByteArrayListener {

        /**
         * Receive incoming data from the client.
         *
         * @return
         *  Whether the data is consumed. Should only return {@code false} if the
         *  data is completely ignored.
         */
        boolean handle(MinecraftServer server, NetServerHandler handler, EntityPlayer player, byte[] data) throws IOException;

    }
}