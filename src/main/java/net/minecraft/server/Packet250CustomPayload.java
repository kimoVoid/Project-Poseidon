package net.minecraft.server;

import net.ornithemc.osl.networking.impl.NetServerHandlerImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet250CustomPayload extends Packet {

    public String channel;
    public int size;
    public byte[] data;

    public Packet250CustomPayload() {
    }

    public Packet250CustomPayload(String channel, byte[] data) {
        this.channel = channel;
        this.data = data;
        if (data != null) {
            this.size = data.length;
            if (this.size > Short.MAX_VALUE) {
                throw new IllegalArgumentException("Payload may not be larger than 32k");
            }
        }
    }

    // the IOException has been stripped from the read/write methods
    // by the obfuscator, thus we catch it and re-throw it as a
    // runtime exception - it will be caught in Connection#read anyhow

    @Override
    public void a(DataInputStream input) {
        try {
            this.channel = a(input, 20);
            this.size = input.readShort();
            if (this.size > 0 && this.size < Short.MAX_VALUE) {
                this.data = new byte[this.size];
                input.readFully(this.data);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void a(DataOutputStream output) {
        try {
            a(this.channel, output);
            output.writeShort(this.size);
            if (this.data != null) {
                output.write(this.data);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void a(NetHandler handler) {
        NetServerHandlerImpl.handle((NetServerHandler)handler, this);
    }

    @Override
    public int a() {
        return 2 + this.channel.length() * 2 + 2 + this.size;
    }
}