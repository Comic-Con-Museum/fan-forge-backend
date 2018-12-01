package org.comic_conmuseum.fan_forge.backend.util;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Consumer;

/**
 * Simple utilities to convert things from normal Java types to SQL types, so
 * that we don't need to do it manually all over the place.
 */
public class SqlTypeConverters {
    public static Timestamp timestampOf(Instant when) {
        return Timestamp.valueOf(when.atZone(ZoneId.systemDefault()).toLocalDateTime());
    }
    
    public static long idToLong(Number key, Consumer<Long> setId) throws SQLException {
        if (key == null) {
            throw new SQLException("Failed to insert rows (no key generated)");
        }
        long id = key.longValue();
        setId.accept(id);
        return id;
    }
}
