package asd.fred.wynncraft_spell_caster;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClickQueue {
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final BlockingQueue<Boolean> queue = new LinkedBlockingQueue<>(); // 0 = left click, 1 = right click

    public ClickQueue(MinecraftClient client) {
        Thread click_thread = new Thread(new ClickExecutor(client));
        click_thread.start();
    }

    public int size() {
        return queue.size();
    }

    public int time_remaining() {
        MinecraftClient client = MinecraftClient.getInstance();

        int time = 0;
        for (boolean click : queue.stream().toList()) {
            if (click ^ Utils.isArcher(client)) {
                time += WynncraftSpellCasterClient.config_data.right_interval_ms;
            } else {
                time += WynncraftSpellCasterClient.config_data.left_interval_ms;
            }
        }
        return time;
    }

    public void add_clicks(Collection<Boolean> clicks) {
        queue.addAll(clicks);
    }

    public void stop() {
        running.set(false);
    }


    private static class ClickExecutor implements Runnable {
        private final MinecraftClient client;

        public ClickExecutor(MinecraftClient client) {
            this.client = client;
        }

        private void sendPacket(Packet<?> packet) {
            ClientPlayNetworkHandler network_handler = client.getNetworkHandler();
            if (network_handler == null)
                WynncraftSpellCasterClient.logger.error("network handler is null");
            else
                network_handler.sendPacket(packet);
        }

        private void sendAttackPacket() {
            sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        private void sendUsePacket() {
            if (client.player == null)
                WynncraftSpellCasterClient.logger.error("player is null");
            else
                sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, client.player.getYaw(), client.player.getPitch()));
        }

        private void execute_next_click(boolean next_click) throws InterruptedException {
            if (client.player != null) {
                if (next_click ^ Utils.isArcher(client)) {
                    sendUsePacket();
                    Thread.sleep(WynncraftSpellCasterClient.config_data.right_interval_ms);
                } else {
                    sendAttackPacket();
                    Thread.sleep(WynncraftSpellCasterClient.config_data.left_interval_ms);
                }
            }
        }

        public void run() {
            while (running.get()) {
                try {
                    execute_next_click(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
