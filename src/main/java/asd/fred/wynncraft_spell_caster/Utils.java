package asd.fred.wynncraft_spell_caster;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class Utils {
    public static boolean isArcher(MinecraftClient client) {
        assert client.player != null;
        ItemStack heldItem = client.player.getMainHandStack();

        List<Text> tooltip = heldItem.getTooltip(client.player, TooltipContext.BASIC);

        for (Text line : tooltip) {
            if (line.contains(Text.literal("Archer/Hunter")))
                return true;
        }
        return false;
    }
}
