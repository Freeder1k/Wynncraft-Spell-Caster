package asd.fred.wynncraft_spell_caster;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class Utils {
    public static boolean isArcher(MinecraftClient client) {
        assert client.player != null;
        ItemStack heldItem = client.player.getMainHandStack();

        List<Text> tooltip = heldItem.getTooltip(Item.TooltipContext.DEFAULT, client.player, TooltipType.BASIC);

        for (Text line : tooltip) {
            if (line.getString().contains("Archer/Hunter"))
                return true;
        }
        return false;
    }
}
