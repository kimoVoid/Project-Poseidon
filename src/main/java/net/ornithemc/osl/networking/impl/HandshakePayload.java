package net.ornithemc.osl.networking.impl;

import net.minecraft.server.Packet;
import net.ornithemc.osl.networking.api.CustomPayload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class HandshakePayload implements CustomPayload {

    public static final String CHANNEL = "OSL|Handshake";

    public Set<String> channels;

    public HandshakePayload() {
    }

    public HandshakePayload(Set<String> channels) {
        this.channels = channels;
    }

    public static HandshakePayload client() {
        throw new UnsupportedOperationException();
    }

    public static HandshakePayload server() {
        return new HandshakePayload(NetServerHandlerImpl.LISTENERS.keySet());
    }

    @Override
    public void read(DataInputStream input) throws IOException {
        channels = new LinkedHashSet<>();
        int channelCount = input.readInt();

        if (channelCount > 0) {
            for (int i = 0; i < channelCount; i++) {
                channels.add(Packet.a(input, 20));
            }
        }
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeInt(channels.size());

        for (String channel : channels) {
            Packet.a(channel, output);
        }
    }
}