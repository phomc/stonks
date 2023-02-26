package dev.phomc.stonks.utils;

import java.util.UUID;

public class UUIDConverter {
	public static String toDashless(UUID uuid) {
		return uuid.toString().replaceAll("\\-", "");
	}

	public static UUID fromDashless(String dashless) {
		String a = dashless.substring(0, 8);
		String b = dashless.substring(8, 12);
		String c = dashless.substring(12, 16);
		String d = dashless.substring(16, 20);
		String e = dashless.substring(20);
		return UUID.fromString(a + "-" + b + "-" + c + "-" + d + "-" + e);
	}
}
