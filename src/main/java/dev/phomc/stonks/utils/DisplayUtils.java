package dev.phomc.stonks.utils;

import java.text.DecimalFormat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DisplayUtils {
	public static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,##0.##");

	public static MutableComponent labeledValue(String label, Double value) {
		return Component.literal(label).withStyle(ChatFormatting.GRAY)
				.append(Component.literal(value != null? PRICE_FORMATTER.format(value) : "Not available!").withStyle(value != null? ChatFormatting.GOLD : ChatFormatting.RED));
	}
}
