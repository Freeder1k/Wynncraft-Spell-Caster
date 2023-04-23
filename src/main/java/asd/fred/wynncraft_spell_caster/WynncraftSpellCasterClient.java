package asd.fred.wynncraft_spell_caster;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final Logger logger = LoggerFactory.getLogger("modid");
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
            //attack_key.setPressed(false);
            left_clicked = false;
        }
        if (right_clicked) {
            //use_key.setPressed(false);
            right_clicked = false;
        }

        if (config_key.isPressed()) {
            config_key.setPressed(false);
            client.setScreen(Config.createConfigScreen(client.currentScreen));
        }

        if (spell1_key.isPressed()) {
            clickQueue.addAll(spell1_clicks);
            spell1_key.setPressed(false);
        }
        if (spell2_key.isPressed()) {
            clickQueue.addAll(spell2_clicks);
            spell2_key.setPressed(false);
        }
        if (spell3_key.isPressed()) {
            clickQueue.addAll(spell3_clicks);
            spell3_key.setPressed(false);
        }
        if (spell4_key.isPressed()) {
            clickQueue.addAll(spell4_clicks);
            spell4_key.setPressed(false);
        }

        if (click_cooldown == 0 && !clickQueue.isEmpty()) {
            boolean next_click = clickQueue.pop();
            if (client.player != null) {
                if (next_click ^ config_data.invert_clicks) {
                    //use_key.setPressed(true);
                    sendUsePacket(client);
                    right_clicked = true;
                    click_cooldown += config_data.right_interval - 1;
                } else {
                    //attack_key.setPressed(true);
                    sendAttackPacket(client);
                    left_clicked = true;
                    click_cooldown += config_data.left_interval - 1;
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

    private void sendAttackPacket(MinecraftClient client) {
        try {
            ClientPlayNetworkHandler network_handler = client.getNetworkHandler();
            if (network_handler != null)
                network_handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            else
                logger.error("network handler is null");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void sendUsePacket(MinecraftClient client) {
        try {
            ClientPlayNetworkHandler network_handler = client.getNetworkHandler();
            if (network_handler != null)
                network_handler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
            else
                logger.error("network handler is null");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
