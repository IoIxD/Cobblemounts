package net.ioixd;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.moandjiezana.toml.Toml;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;

public class Config implements FabricPacket {

    public boolean groundCappedSpeed = true;
    public boolean groundUseLogScaling = true;
    public double groundSpeedScalar = 150.0d;
    public double groundSpeedCap = 3.0d;

    public boolean flightCappedSpeed = true;
    public boolean flightUseLogScaling = true;
    public double flightSpeedScalar = 300.0d;
    public double flightSpeedCap = 2.0d;

    public boolean swimCappedSpeed = true;
    public boolean swimUseLogScaling = true;
    public double swimSpeedScalar = 75.0d;
    public double swimSpeedCap = 4.0d;

    public double legendaryModifier = 0.5d;
    public boolean legendaryModifierCapBreak = true;

    public boolean allowFlying = true;
    public boolean allowSwimming = true;

    public List<String> list = new ArrayList<>();
    public List<String> alsoFlyList = new ArrayList<>();

    public ListUse listUse;

    @Override
    public void write(PacketByteBuf p) {

        p.writeBoolean(groundCappedSpeed);
        p.writeBoolean(groundUseLogScaling);
        p.writeDouble(groundSpeedScalar);
        p.writeDouble(groundSpeedCap);

        p.writeBoolean(flightCappedSpeed);
        p.writeBoolean(flightUseLogScaling);
        p.writeDouble(flightSpeedScalar);
        p.writeDouble(flightSpeedCap);

        p.writeBoolean(swimCappedSpeed);
        p.writeBoolean(swimUseLogScaling);
        p.writeDouble(swimSpeedScalar);
        p.writeDouble(swimSpeedCap);

        p.writeDouble(legendaryModifier);
        p.writeBoolean(legendaryModifierCapBreak);
        
        p.writeBoolean(allowFlying);
        p.writeBoolean(allowSwimming);
        p.writeEnumConstant(listUse);

        int size = list.size();
        p.writeInt(size);
        for (String value : list) {
            p.writeString(value);
        }
        size = alsoFlyList.size();
        p.writeInt(size);
        for (String value : alsoFlyList) {
            p.writeString(value);
        }
    }

    private PacketType<Config> SYNC = PacketType.create(Cobblemounts.CONFIG_SYNC_ID, Config::read);

    @Override
    public PacketType<?> getType() {
        return SYNC;
    }

    public enum ListUse {
        NONE,
        WHITELIST,
        BLACKLIST
    }

    public Config() {
        try {
            this.update();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void update() throws Exception {
        // Get the file or unzip the default.
        Path configDir = Paths.get(FabricLoader.getInstance().getConfigDir().toString(), "cobblemounts.toml");

        FileReader ok;
        try {
            ok = new FileReader(configDir.toFile());
        } catch (FileNotFoundException e) {
            try {
                configDir.toFile().createNewFile();
                FileWriter newFile = new FileWriter(configDir.toFile());
                InputStream in = this.getClass().getResourceAsStream("/cobblemounts.toml");
                if (in == null) {
                    throw new FileNotFoundException();
                }
                String line;
                try (
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        BufferedWriter writer = new BufferedWriter(newFile);) {
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        // must do this: .readLine() will have stripped line endings
                        writer.newLine();
                    }
                }
                // Use resource
                ok = new FileReader(configDir.toFile());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        Toml toml;
        try (
                BufferedReader reader = new BufferedReader(ok);) {
            toml = new Toml().read(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        this.groundCappedSpeed = toml.getBoolean("groundCappedSpeed", true);
        this.groundUseLogScaling = toml.getBoolean("groundUseLogScaling", false);
        this.groundSpeedScalar = toml.getDouble("groundSpeedScalar", 150.0d);
        this.groundSpeedCap = toml.getDouble("groundSpeedCap", 3.0d);

        this.flightCappedSpeed = toml.getBoolean("flightCappedSpeed", true);
        this.flightUseLogScaling = toml.getBoolean("flightUseLogScaling", true);
        this.flightSpeedScalar = toml.getDouble("flightSpeedScalar", 300.0d);
        this.flightSpeedCap = toml.getDouble("flightSpeedCap", 2.0d);

        this.swimCappedSpeed = toml.getBoolean("swimCappedSpeed", true);
        this.swimUseLogScaling = toml.getBoolean("swimUseLogScaling", true);
        this.swimSpeedScalar = toml.getDouble("swimSpeedScalar", 75.0d);
        this.swimSpeedCap = toml.getDouble("swimSpeedCap", 4.0d);

        this.legendaryModifier = toml.getDouble("legenedaryModifier", 0.5d);
        this.legendaryModifierCapBreak = toml.getBoolean("legenedaryModifierCapBreak", true);

        this.allowFlying = true;
        this.allowSwimming = true;

        String listUse = toml.getString("listUse", "").toLowerCase();
        switch (listUse) {
            case "blacklist":
                this.listUse = ListUse.BLACKLIST;
                break;
            case "whitelist":
                this.listUse = ListUse.WHITELIST;
                break;
            default:
                this.listUse = ListUse.NONE;
                break;
        }
        this.list = toml.getList("list", new ArrayList<String>()).stream().map(f -> {
            return f.toLowerCase();
        }).toList();
        this.alsoFlyList = toml.getList("alsoFlying", new ArrayList<String>()).stream().map(f -> {
            return f.toLowerCase();
        }).toList();
    }

    public static Config read(PacketByteBuf p) {
        var config = new Config();
        
        config.groundCappedSpeed = p.readBoolean();
        config.groundUseLogScaling = p.readBoolean();
        config.groundSpeedScalar = p.readDouble();
        config.groundSpeedCap = p.readDouble();

        config.flightCappedSpeed = p.readBoolean();
        config.flightUseLogScaling = p.readBoolean();
        config.flightSpeedScalar = p.readDouble();
        config.flightSpeedCap = p.readDouble();

        config.swimCappedSpeed = p.readBoolean();
        config.swimUseLogScaling = p.readBoolean();
        config.swimSpeedScalar = p.readDouble();
        config.swimSpeedCap = p.readDouble();

        config.swimSpeedCap = p.readDouble();
        config.legendaryModifierCapBreak = p.readBoolean();
        
        config.allowFlying = p.readBoolean();
        config.allowSwimming = p.readBoolean();

        config.listUse = p.readEnumConstant(ListUse.class);
        int s = p.readInt();
        config.list = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            var str = p.readString();
            config.list.add(str);
        }
        s = p.readInt();
        config.alsoFlyList = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            var str = p.readString();
            config.alsoFlyList.add(str);
        }
        return config;
    }
}
