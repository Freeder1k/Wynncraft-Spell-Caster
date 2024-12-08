package asd.fred.wynncraft_spell_caster;

import com.google.gson.Gson;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private static final Path config_dir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath() + "/config");
    private static final Path config_file = Paths.get(config_dir + "/wynncraft-spell-caster.json");
    private static ConfigData config_data;

    public static ConfigData getConfigData() {
        if (config_data != null) return config_data;

        try {
            if (!Files.exists(config_file)) {
                Files.createDirectories(config_dir);
                Files.createFile(config_file);
                config_data = ConfigData.getDefault();
                config_data.save();
                return config_data;
            }
        } catch (IOException e) {
            e.printStackTrace();
            config_data = ConfigData.getDefault();
            return config_data;
        }
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(config_file.toFile());
            config_data = gson.fromJson(reader, ConfigData.class);
        } catch (IOException e) {
            e.printStackTrace();
            config_data = ConfigData.getDefault();
        }
        return config_data;
    }

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("screen.wynncraft-spell-caster.config.title"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("General Config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startIntField(Text.translatable("config.wynncraft-spell-caster.option.left-interval_ms"), config_data.left_interval_ms).setDefaultValue(100).setTooltip(Text.of("Set the amount of milliseconds to wait after a left click.")).setSaveConsumer(newValue -> config_data.left_interval_ms = newValue).build());

        general.addEntry(entryBuilder.startIntField(Text.translatable("config.wynncraft-spell-caster.option.right-interval_ms"), config_data.right_interval_ms).setDefaultValue(100).setTooltip(Text.of("Set the amount of milliseconds to wait after a right click.")).setSaveConsumer(newValue -> config_data.right_interval_ms = newValue).build());

        builder.setSavingRunnable(config_data::save);

        return builder.build();
    }

    public static class ConfigData {
        public int left_interval_ms;
        public int right_interval_ms;

        public ConfigData(int left_interval_ms, int right_interval_ms, boolean invert_clicks) {
            this.left_interval_ms = left_interval_ms;
            this.right_interval_ms = right_interval_ms;
        }

        public static ConfigData getDefault() {
            return new ConfigData(100, 100, false);
        }

        public void save() {
            try {
                Gson gson = new Gson();
                FileWriter writer = new FileWriter(config_file.toFile());
                gson.toJson(this, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
