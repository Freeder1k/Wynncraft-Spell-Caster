package asd.fred.wynncraft_spell_caster;

import com.google.gson.Gson;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config implements ModMenuApi {
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

        general.addEntry(builder.entryBuilder().startIntSlider(Text.translatable("config.wynncraft-spell-caster.option.left-interval"), config_data.left_interval, 0, 20).setDefaultValue(0).setTooltip(Text.of("Set the amount of ticks to wait after a left click.")).setSaveConsumer(newValue -> config_data.left_interval = newValue).build());

        general.addEntry(builder.entryBuilder().startIntSlider(Text.translatable("config.wynncraft-spell-caster.option.right-interval"), config_data.right_interval, 0, 20).setDefaultValue(2).setTooltip(Text.of("Set the amount of ticks to wait after a right click.")).setSaveConsumer(newValue -> config_data.right_interval = newValue).build());

        general.addEntry(builder.entryBuilder().startIntSlider(Text.translatable("config.wynncraft-spell-caster.option.queue-size"), config_data.queue_size, 0, 20).setDefaultValue(1).setTooltip(Text.of("Set how many extra spells the spell queue can hold.")).setSaveConsumer(newValue -> config_data.queue_size = newValue).build());

        general.addEntry(builder.entryBuilder().startBooleanToggle(Text.translatable("config.wynncraft-spell-caster.option.invert-clicks"), config_data.invert_clicks).setDefaultValue(false).setTooltip(Text.of("Set this to True if playing on archer.")).setSaveConsumer(newValue -> config_data.invert_clicks = newValue).build());

        builder.setSavingRunnable(config_data::save);

        return builder.build();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Config::createConfigScreen;
    }

    public static class ConfigData {
        public int left_interval;
        public int right_interval;
        public int queue_size;
        public boolean invert_clicks;

        public ConfigData(int left_interval, int right_interval, int queue_size, boolean invert_clicks) {
            this.left_interval = left_interval;
            this.right_interval = right_interval;
            this.queue_size = queue_size;
            this.invert_clicks = invert_clicks;
        }

        public static ConfigData getDefault() {
            return new ConfigData(0, 2, 2,false);
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
