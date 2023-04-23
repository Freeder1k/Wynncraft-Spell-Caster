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
    private static final int left_interval = 0;
    private static final int right_interval = 1;
    public static KeyBinding left;
    public static KeyBinding right;
    private static KeyBinding spell1, spell2, spell3, spell4;
    private static int click_cooldown = 0;
    private static boolean left_clicked = false, right_clicked = false;
    private final List<Boolean> spell1_clicks = Arrays.asList(true, false, true);
    private final List<Boolean> spell2_clicks = Arrays.asList(true, true, true);
    private final List<Boolean> spell3_clicks = Arrays.asList(true, false, false);
    private final List<Boolean> spell4_clicks = Arrays.asList(true, true, false);

    @Override
    public void onInitializeClient() {
        registerKeybinds();
        ClientLifecycleEvents.CLIENT_STARTED.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }

    @Override
    public void onClientStarted(MinecraftClient client) {
        left = client.options.attackKey;
        right = client.options.useKey;
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (left_clicked) {
            left.setPressed(false);
            left_clicked = false;
        }
        if (right_clicked) {
            right.setPressed(false);
            right_clicked = false;
        }

        while (spell1.wasPressed()) {
            clickQueue.addAll(spell1_clicks);
        }
        while (spell2.wasPressed()) {
            clickQueue.addAll(spell2_clicks);
        }
        while (spell3.wasPressed()) {
            clickQueue.addAll(spell3_clicks);
        }
        while (spell4.wasPressed()) {
            clickQueue.addAll(spell4_clicks);
        }

        if (click_cooldown == 0) {
            if (!clickQueue.isEmpty()) {
                boolean next_click = clickQueue.pop();
                if (client.player != null) {
                    if (next_click) {
                        left.setPressed(true);
                        left_clicked = true;
                        click_cooldown += left_interval;
                    } else {
                        right.setPressed(true);
                        right_clicked = true;
                        click_cooldown += right_interval;
                    }
                }
            }
        }

        if (click_cooldown > 0) click_cooldown--;
    }

    private void registerKeybinds() {
        spell1 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.first", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell2 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.second", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell3 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.third", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell4 = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.fourth", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
    }
}
