package dev.phomc.stonks.utils;

import java.text.DecimalFormat;
import java.util.function.UnaryOperator;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class DisplayUtils {
	public static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("#,##0.##");

	public static MutableComponent labeledValue(String label, Double value) {
		return Component.literal(label).withStyle(ChatFormatting.GRAY)
				.append(Component.literal(value != null? PRICE_FORMATTER.format(value) : "Not available!").withStyle(value != null? ChatFormatting.GOLD : ChatFormatting.RED));
	}

	public static final String PROGRESS_BAR_SPACES;
	static {
		String spaces = "";
		while (spaces.length() < 20) spaces += " ";
		PROGRESS_BAR_SPACES = spaces;
	}

	public static MutableComponent progressBar(double prog, UnaryOperator<Style> progressed, UnaryOperator<Style> pending) {
		int progChars = (int) Math.round(PROGRESS_BAR_SPACES.length() * prog);
		MutableComponent base = Component.empty().withStyle(s -> s.withStrikethrough(true));
		base.append(Component.literal(PROGRESS_BAR_SPACES.substring(0, progChars)).withStyle(progressed));
		base.append(Component.literal(PROGRESS_BAR_SPACES.substring(progChars)).withStyle(pending));
		return base;
	}

	public static MutableComponent progressBar(double prog) {
		return progressBar(prog, s -> s.withColor(prog <= 0.33? ChatFormatting.RED : prog <= 0.66? ChatFormatting.YELLOW : ChatFormatting.GREEN), s -> s.withColor(ChatFormatting.DARK_GRAY));
	}
}
