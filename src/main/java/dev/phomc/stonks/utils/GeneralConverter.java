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
