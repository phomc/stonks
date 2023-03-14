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

import java.util.Map;
import java.util.UUID;

public class GeneralConverter {
	public static String uuidToDashless(UUID uuid) {
		return uuid.toString().replaceAll("\\-", "");
	}

	public static UUID uuidFromDashless(String dashless) {
		String a = dashless.substring(0, 8);
		String b = dashless.substring(8, 12);
		String c = dashless.substring(12, 16);
		String d = dashless.substring(16, 20);
		String e = dashless.substring(20);
		return UUID.fromString(a + "-" + b + "-" + c + "-" + d + "-" + e);
	}

	private static final Map<Character, Double> MONEY_SUFFIXES = Map.of(
			'k', 1_000D,
			'm', 1_000_000D,
			'b', 1_000_000_000D,
			'K', 1_000D,
			'M', 1_000_000D,
			'B', 1_000_000_000D
			);

	public static double moneyWithSuffix(String input) {
		if (input.length() == 0) throw new NumberFormatException("0-length string");

		char k = input.charAt(input.length() - 1);
		double mul = MONEY_SUFFIXES.getOrDefault(k, 1D);
		if (mul != 1D) input = input.substring(0, input.length() - 1);

		return Double.parseDouble(input) * mul;
	}
}
