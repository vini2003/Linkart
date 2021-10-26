package com.github.vini2003.linkart.item;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class LinkerItem extends Item {
	public LinkerItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		if (!Screen.hasShiftDown()) {
			tooltip.add(new TranslatableText("text.linkart.item.show_more").formatted(Formatting.ITALIC, Formatting.GREEN));
		} else {
			tooltip.add(new TranslatableText("text.linkart.item.showing_more_parent").formatted(Formatting.ITALIC, Formatting.BLUE));
			tooltip.add(new TranslatableText("text.linkart.item.showing_more_child").formatted(Formatting.ITALIC, Formatting.BLUE));
			tooltip.add(new TranslatableText("text.linkart.item.showing_more_conclusion").formatted(Formatting.ITALIC, Formatting.BLUE));
		}
	}
}
