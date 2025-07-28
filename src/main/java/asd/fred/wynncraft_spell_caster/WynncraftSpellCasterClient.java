package asd.fred.wynncraft_spell_caster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WynncraftSpellCasterClient implements ClientModInitializer, ClientLifecycleEvents.ClientStarted, ClientLifecycleEvents.ClientStopping, ClientTickEvents.EndTick {
    public static final Logger logger = LoggerFactory.getLogger("wynncraft-spell-caster");
    private static final List<Boolean> spell1_clicks = Arrays.asList(true, false, true);
    private static final List<Boolean> spell2_clicks = Arrays.asList(true, true, true);
    private static final List<Boolean> spell3_clicks = Arrays.asList(true, false, false);
    private static final List<Boolean> spell4_clicks = Arrays.asList(true, true, false);
    private static final List<Boolean> melee_clicks = List.of(false);
    private static boolean check_melee = true;
    public static KeyBinding spell1_key, spell2_key, spell3_key, spell4_key, melee_key;
    public static KeyBinding config_key;
    public static Config.ConfigData config_data;
    public static ClickQueue clickQueue;

    @Override
    public void onInitializeClient() {
        config_data = Config.getConfigData();
        registerKeybinds();
        ClientLifecycleEvents.CLIENT_STARTED.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this);
    }

    @Override
    public void onClientStarted(MinecraftClient client) {
        clickQueue = new ClickQueue(client);
    }

    @Override
    public void onClientStopping(MinecraftClient client) {
        clickQueue.stop();
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (config_key.isPressed()) {
            config_key.setPressed(false);
            client.setScreen(Config.createConfigScreen(client.currentScreen));
        }

        checkSpellKey(spell1_key, spell1_clicks, false);
        checkSpellKey(spell2_key, spell2_clicks, false);
        checkSpellKey(spell3_key, spell3_clicks, false);
        checkSpellKey(spell4_key, spell4_clicks, false);
        if (check_melee)
            checkSpellKey(melee_key, melee_clicks, config_data.repeat_melee);
        // Wait a tick between melee checks
        if (melee_key.isPressed())
            check_melee = !check_melee;
        else
            check_melee = true;
    }

    private void registerKeybinds() {
        config_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));

        spell1_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.first", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell2_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.second", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell3_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.third", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell4_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.fourth", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        melee_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.melee", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
    }

    private void checkSpellKey(KeyBinding spell_key, Collection<Boolean> spell_clicks, boolean repeatable) {
        if (spell_key.isPressed()) {
            if (clickQueue.isEmpty())
                clickQueue.add_clicks(spell_clicks);

            if (!repeatable)
                spell_key.setPressed(false);
        }
    }
}
