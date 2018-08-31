package de.otto.edison.testsupport.util;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.Instant.parse;
import static java.util.Collections.emptyMap;
import static java.util.Date.from;
import static java.util.stream.Collectors.toList;

public class JsonMap {

    private final Object jsonObject;

    private JsonMap(final Object jsonObject) {
        this.jsonObject = jsonObject;
    }

    public static JsonMap jsonMapFrom(final Map<?,?> map) {
        return new JsonMap(map);
    }

    public static JsonMap jsonMapFrom(final Object map) {
        if (map instanceof JsonMap) {
            return (JsonMap) map;
        } else {
            return new JsonMap(map);
        }
    }

    public void remove(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            ((Map) jsonObject).remove(key);
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public JsonMap get(final String s) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            String[] segments = s.split("\\.");
            if (segments.length < 2) {
                final Object object = ((Map) jsonObject).get(s);
                return object != null ? new JsonMap(object) : null;
            } else {
                return get(segments[0]).get(s.substring(s.indexOf(".")+1));
            }
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public String getString(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final Object value = get(key);
            if (value == null) {
                return null;
            } else {
                return value.toString();
            }
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public String getString(final String key, final String defaultValue) {
        final String result = getString(key);
        return result != null ? result : defaultValue;
    }

    public Boolean getBoolean(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final Object value = get(key);
            return value != null ? Boolean.valueOf(value.toString()) : null;
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        final Boolean result = getBoolean(key);
        return result != null ? result : defaultValue;
    }

    public Integer getInt(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final Object value = get(key);
            return value != null ? Integer.valueOf(value.toString()) : null;
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public Integer getInt(final String key, final Integer defaultValue) {
        final Integer result = getInt(key);
        return result != null ? result : defaultValue;
    }

    public Long getLong(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final Object value = get(key);
            return value != null ? Long.valueOf(value.toString()) : null;
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public long getLong(final String key, final long defaultValue) {
        final Long result = getLong(key);
        return result != null ? result : defaultValue;
    }

    public Float getFloat(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final Object value = get(key);
            return value != null ? Float.valueOf(value.toString()) : null;
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public Float getFloat(final String key, final float defaultValue) {
        final Float result = getFloat(key);
        return result != null ? result : defaultValue;
    }

    public Double getDouble(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final Object value = get(key);
            return value != null ? Double.valueOf(value.toString()) : null;
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public Double getDouble(final String key, final double defaultValue) {
        final Double result = getDouble(key);
        return result != null ? result : defaultValue;
    }

    public Object getObject(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            return get(key);
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public Date getDate(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final JsonMap value = get(key);
            if (value == null) {
                return null;
            } else {
                if (value.is(Date.class)) {
                    return (Date) value.jsonObject;
                } if (value.is(String.class)) {
                    return from(parse(value.toString()));
                } else {
                    throw new ClassCastException("Value of " + key + " is not a date but a " + value.getClass().getSimpleName());
                }
            }
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    public Instant getInstant(final String key) {
        if (jsonObject == null) {
            throw new NullPointerException("json object is null");
        }
        if (is(Map.class)) {
            final String value = getString(key);
            return value != null ? parse(value) : null;
        } else {
            throw new IllegalArgumentException("not a map but a " + jsonObject.getClass().getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> asListOf(final Class<T> type) {
        if (jsonObject == null) {
            return null;
        }
        if (is(List.class)) {
            if (JsonMap.class.isAssignableFrom(type)) {
                return (List<T>) ((List)jsonObject).stream().map(JsonMap::new).collect(toList());
            } else {
                return ((List) jsonObject);
            }
        } else {
            throw new ClassCastException("not a collection but a " + jsonObject.getClass().getSimpleName());
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T> Set<T> asSetOf(final Class<T> type) {
        if (jsonObject == null) {
            return null;
        }
        if (is(Collection.class)) {
            return new HashSet<T>((Collection) jsonObject);
        } else {
            throw new ClassCastException("not a collection but a " + jsonObject.getClass().getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> asMap() {
        if (jsonObject == null) {
            return emptyMap();
        }
        if (is(Map.class)) {
            return (Map<String, Object>) jsonObject;
        }
        throw new IllegalStateException("not a map but a " + jsonObject.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public <T> Map<T, Object> asMapWithKeysOfType(final Class<T> type) {
        if (jsonObject == null) {
            return emptyMap();
        }
        if (is(Map.class)) {
            return (Map<T, Object>) jsonObject;
        }
        throw new IllegalStateException("not a map but a " + jsonObject.getClass().getSimpleName());
    }


    @SuppressWarnings("unchecked")
    public Set<String> keySet() {
        if (jsonObject == null) {
            return Collections.emptySet();
        }
        if (is(Map.class)) {
            return ((Map) jsonObject).keySet();
        }
        throw new IllegalStateException("not a map but a " + jsonObject.getClass().getSimpleName());

    }

    private boolean is(final Class<?> type) {
        return type.isAssignableFrom(jsonObject.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonMap jsonMap = (JsonMap) o;

        if (jsonObject != null ? !jsonObject.equals(jsonMap.jsonObject) : jsonMap.jsonObject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return jsonObject != null ? jsonObject.hashCode() : 0;
    }

    public String toString() {
        return jsonObject.toString();
    }
}