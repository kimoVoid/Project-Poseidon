package wtf.basico.networking.payload;

import net.ornithemc.osl.networking.api.CustomPayload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FlightPayload implements CustomPayload {

    public boolean flying;

    public FlightPayload() {}

    public FlightPayload(boolean flying) {
        this.flying = flying;
    }

    @Override
    public void read(DataInputStream input) {
        try {
            this.flying = input.readBoolean();
        } catch (IOException ignored) {}
    }

    @Override
    public void write(DataOutputStream output) {
        try {
            output.writeBoolean(this.flying);
        } catch (IOException ignored) {}
    }
}