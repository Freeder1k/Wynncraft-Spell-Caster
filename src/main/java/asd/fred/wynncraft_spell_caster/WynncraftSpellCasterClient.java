package asd.fred.wynncraft_spell_caster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WynncraftSpellCasterClient implements ClientModInitializer, ClientLifecycleEvents.ClientStarted, ClientLifecycleEvents.ClientStopping, ClientTickEvents.EndTick {


    public static final Logger logger = LoggerFactory.getLogger("wynncraft-spell-caster");
    private static final BlockingQueue<Boolean> clickQueue = new LinkedBlockingQueue<>(); // 0 = left click, 1 = right click
    private static final List<Boolean> spell1_clicks = Arrays.asList(true, false, true);
    private static final List<Boolean> spell2_clicks = Arrays.asList(true, true, true);
    private static final List<Boolean> spell3_clicks = Arrays.asList(true, false, false);
    private static final List<Boolean> spell4_clicks = Arrays.asList(true, true, false);
    public static KeyBinding attack_key;
    public static KeyBinding use_key;
    private static KeyBinding spell1_key, spell2_key, spell3_key, spell4_key;
    private static KeyBinding config_key;
    private static Config.ConfigData config_data;
    private static final AtomicBoolean running = new AtomicBoolean(true);


    private static void sendAttackPacket(MinecraftClient client) {
        ClientPlayNetworkHandler network_handler = client.getNetworkHandler();
        if (network_handler != null) network_handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        else logger.error("network handler is null");
    }

    private static void sendUsePacket(MinecraftClient client) {
        ClientPlayNetworkHandler network_handler = client.getNetworkHandler();
        if (network_handler != null) network_handler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
        else logger.error("network handler is null");
    }

    private static void execute_next_click(MinecraftClient client, boolean next_click) throws InterruptedException {
        if (client.player != null) {
            if (next_click ^ config_data.invert_clicks) {
                sendUsePacket(client);
                Thread.sleep(config_data.right_interval_ms);
            } else {
                sendAttackPacket(client);
                Thread.sleep(config_data.left_interval_ms);
            }
        }
    }

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
        attack_key = client.options.attackKey;
        use_key = client.options.useKey;
        Thread click_thread = new Thread(new ClickExecutor(client));
        click_thread.start();
    }

    @Override
    public void onClientStopping(MinecraftClient client) {
        running.set(false);
    }
    @Override
    public void onEndTick(MinecraftClient client) {
        if (config_key.isPressed()) {
            config_key.setPressed(false);
            client.setScreen(Config.createConfigScreen(client.currentScreen));
        }

        checkSpellKey(spell1_key, spell1_clicks);
        checkSpellKey(spell2_key, spell2_clicks);
        checkSpellKey(spell3_key, spell3_clicks);
        checkSpellKey(spell4_key, spell4_clicks);
    }

    private void registerKeybinds() {
        config_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));

        spell1_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.first", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell2_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.second", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell3_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.third", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
        spell4_key = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wynncraft-spell-caster.spell.fourth", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.spell"));
    }

    private void checkSpellKey(KeyBinding spell_key, List<Boolean> spell_clicks) {
        if (spell_key.isPressed()) {
            if (clickQueue.size() <= 3)
                clickQueue.addAll(spell_clicks);

            spell_key.setPressed(false);
        }
    }

    private static class ClickExecutor implements Runnable {
        private final MinecraftClient client;

        public ClickExecutor(MinecraftClient client) {
            this.client = client;
        }

        public void run() {
            while (running.get()) {
                try {
                    execute_next_click(client, clickQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
