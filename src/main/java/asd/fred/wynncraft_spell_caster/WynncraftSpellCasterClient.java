package asd.fred.wynncraft_spell_caster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WynncraftSpellCasterClient implements ClientModInitializer, ClientLifecycleEvents.ClientStarted, ClientTickEvents.EndTick {
    private static final LinkedList<Boolean> clickQueue = new LinkedList<>();  // 0 = left click, 1 = right click
    private static final List<Boolean> spell1_clicks = Arrays.asList(true, false, true);
    private static final List<Boolean> spell2_clicks = Arrays.asList(true, true, true);
    private static final List<Boolean> spell3_clicks = Arrays.asList(true, false, false);
    private static final List<Boolean> spell4_clicks = Arrays.asList(true, true, false);
    public static KeyBinding attack_key;
    public static KeyBinding use_key;
    private static KeyBinding spell1_key, spell2_key, spell3_key, spell4_key;
    private static KeyBinding config_key;
    private static int click_cooldown = 0;
    private static boolean left_clicked = false, right_clicked = false;
    private static Config.ConfigData config_data;

    @Override
    public void onInitializeClient() {
        config_data = Config.getConfigData();
        registerKeybinds();
        ClientLifecycleEvents.CLIENT_STARTED.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }

    @Override
    public void onClientStarted(MinecraftClient client) {
        attack_key = client.options.attackKey;
        use_key = client.options.useKey;
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (left_clicked) {
            attack_key.setPressed(false);
            left_clicked = false;
        }
        if (right_clicked) {
            use_key.setPressed(false);
            right_clicked = false;
        }

        if (config_key.isPressed()) {
            config_key.setPressed(false);
            client.setScreen(Config.createConfigScreen(client.currentScreen));
        }

        while (spell1_key.wasPressed()) {
            clickQueue.addAll(spell1_clicks);
        }
        while (spell2_key.wasPressed()) {
            clickQueue.addAll(spell2_clicks);
        }
        while (spell3_key.wasPressed()) {
            clickQueue.addAll(spell3_clicks);
        }
        while (spell4_key.wasPressed()) {
            clickQueue.addAll(spell4_clicks);
        }

        if (click_cooldown == 0 && !clickQueue.isEmpty()) {
            boolean next_click = clickQueue.pop();
            if (client.player != null) {
                if (next_click ^ config_data.invert_clicks) {
                    attack_key.setPressed(true);
                    left_clicked = true;
                    click_cooldown += config_data.left_interval - 1;
                } else {
                    use_key.setPressed(true);
                    right_clicked = true;
                    click_cooldown += config_data.right_interval - 1;
                }
            }
        }

        if (click_cooldown > 0) click_cooldown--;
    }

    private void registerKeybinds() {
        config_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell1_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.first", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell2_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.second", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell3_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.third", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell4_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.fourth", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
    }
}
