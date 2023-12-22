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

    public boolean cappedSpeed = true;
    public double speedCap = 0.15;
    public double flyingSpeedCap = 0.15;
    public double legendaryModifier = 0.05;

    public boolean allowFlying = true;
    public boolean allowSwimming = true;

    public List<String> list = new ArrayList<>();
    public List<String> alsoFlyList = new ArrayList<>();

    public ListUse listUse;

    @Override
    public void write(PacketByteBuf p) {
        p.writeBoolean(cappedSpeed);
        p.writeDouble(speedCap);
        p.writeDouble(legendaryModifier);
        p.writeDouble(flyingSpeedCap);
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
    private PacketType<Config> SYNC = PacketType.create(Cobblemounts.CONFIG_SYNC_ID,Config::read);
    @Override
    public PacketType<?> getType() {
        return SYNC;
    }

    public enum ListUse {
        NONE,
        WHITELIST,
        BLACKLIST
    }

    Config() {
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

        this.cappedSpeed = toml.getBoolean("cappedSpeed", true);
        this.speedCap = toml.getDouble("speedCap", 0.15);
        this.legendaryModifier = toml.getDouble("legenedaryModifier", 0.05);
        this.flyingSpeedCap = toml.getDouble("flyingSpeedCap", 0.15);
        this.allowFlying = toml.getBoolean("allowFlying", true);
        this.allowSwimming = toml.getBoolean("allowSwimming", true);
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
    public static Config read(PacketByteBuf p){
        var config = new Config();
        config.cappedSpeed = p.readBoolean();
        config.speedCap = p.readDouble();
        config.legendaryModifier = p.readDouble();
        config.flyingSpeedCap = p.readDouble( );
        config.allowFlying = p.readBoolean();
        config.allowSwimming = p.readBoolean();
        config.listUse = p.readEnumConstant(ListUse.class);
        int s = p.readInt();
        config.list= new ArrayList<>(s);
        for(int i=0;i<s;i++) {
            var str = p.readString();
            config.list.add(str);
        }
        s = p.readInt();
        config.alsoFlyList= new ArrayList<>(s);
        for(int i=0;i<s;i++) {
            var str = p.readString();
            config.alsoFlyList.add(str);
        }
        return config;
    }
}
