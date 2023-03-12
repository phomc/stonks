package dev.phomc.stonks.services.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import dev.phomc.stonks.services.StonksService;

public class DatabaseServicePicker {
	private static final Pattern PATTERN = Pattern.compile("(?<key>\\w+)\\s*=\\s*(?<value>.+?)(;|$)");

	private static Map<String, String> mapFromString(String str) {
		Matcher matcher = PATTERN.matcher(str);
		Map<String, String> map = new HashMap<>();

		while (matcher.find()) {
			String key = matcher.group("key");
			String value = matcher.group("value");
			map.put(key, value);
		}

		return Collections.unmodifiableMap(map);
	}

	public static StonksService serviceFromDatabase(String str) {
		Map<String, String> map = mapFromString(str);
		String type = map.get("type");
		String host = map.getOrDefault("host", "localhost");
		if (type == null) throw new IllegalArgumentException("'type' is missing in database config string");

		switch (type) {
		case "mongodb":
		case "mongo": return mongoDBService(host, map);
		default: throw new IllegalArgumentException("Unknown database type: " + type);
		}
	}

	private static MongoDBService mongoDBService(String host, Map<String, String> map) {
		if (!host.startsWith("mongodb://") && !host.startsWith("mongodb+srv://")) host = "mongodb://" + host;

		String database = map.get("database");
		String collection = map.getOrDefault("collection", "stonks_offers");
		if (database == null) throw new IllegalArgumentException("'database' is missing in database config string");

		MongoClient client = MongoClients.create();
		MongoDatabase db = client.getDatabase(database);
		MongoCollection<Document> col = db.getCollection(collection);

		MongoDBService service = new MongoDBService(col);
		service.onClose = () -> client.close();

		return service;
	}
}
