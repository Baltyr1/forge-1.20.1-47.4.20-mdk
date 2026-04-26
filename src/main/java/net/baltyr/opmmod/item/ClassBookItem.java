package net.baltyr.opmmod.item;

import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClassBookItem extends Item {

    public ClassBookItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.literal("§6=== §lChoix de Classe OPM §r§6==="));

            for (OpmClass cls : OpmClass.values()) {
                if (cls == OpmClass.NONE)
                    continue;

                MutableComponent btn = Component.literal("  » " + cls.getFormattedName())
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/opmclass set " + sp.getName().getString() + " " + cls.name().toLowerCase()))
                                .withUnderlined(true));

                sp.sendSystemMessage(btn);
            }

            sp.sendSystemMessage(Component.literal("§7Clique sur une classe pour la sélectionner."));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}