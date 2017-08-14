package de.otto.edison.dynamodb;

class TestObject {

    private final String id;
    private final String value;

    private final String eTag;

    TestObject(final String id, final String value, final String eTag) {
        this.eTag = eTag;
        this.id = id;
        this.value = value;
    }

    TestObject(final String id, final String value) {
        this(id, value, null);
    }

    String getValue() {
        return value;
    }

    String getId() {
        return id;
    }

    String geteTag() {
        return eTag;
    }
}
