package de.otto.edison.dynamodb;

public class TestObject {

    private final String id;
    private final String value;

    private final String eTag;

    protected TestObject(final String id, final String value, final String eTag) {
        this.eTag = eTag;
        this.id = id;
        this.value = value;
    }

    protected TestObject(final String id, final String value) {
        this(id, value, null);
    }

    public String getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    public String geteTag() {
        return eTag;
    }
}
