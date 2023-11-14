package net.ioixd;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.moandjiezana.toml.Toml;
import dev.architectury.platform.Platform;

public class Config {

    public boolean cappedSpeed = true;
    public double speedCap = 0.15;
    public double flyingSpeedCap = 0.15;
    public double legendaryModifier = 0.05;

    public boolean allowFlying = true;
    public boolean allowSwimming = true;

    public List<String> list = new ArrayList<>();

    public ListUse listUse;

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
        Path configDir = Paths.get(Platform.getConfigFolder().toString(), "cobblemounts.toml");

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
    }

}
