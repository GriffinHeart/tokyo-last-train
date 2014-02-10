package com.tokyolasttrain;

import java.lang.reflect.Type;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

	@Override
	public JsonElement serialize(final LocalDateTime src, final Type typeOfSrc, final JsonSerializationContext context) {
		final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return new JsonPrimitive(fmt.print(src.toDateTime()));
	}
	
	@Override
	public LocalDateTime deserialize(JsonElement json, Type typeOfSrc,
			JsonDeserializationContext context) throws JsonParseException {
		final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return fmt.parseDateTime(json.getAsString()).toLocalDateTime();
	}
}
