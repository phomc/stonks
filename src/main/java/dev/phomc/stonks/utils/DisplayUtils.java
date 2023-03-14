/*
 * Copyright (c) 2023 PhoMC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
