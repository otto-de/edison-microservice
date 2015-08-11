package de.otto.edison.mongo.etag;

import java.util.UUID;

public abstract class ETaggable {

    protected final String eTag;

    protected ETaggable(String eTag) {
        this.eTag = eTag;
    }

    protected String createETag() {
        return UUID.randomUUID().toString();
    }

    public abstract <V extends ETaggable> V copyAndAddETag();

    public String getETag() {
        return eTag;
    }
}
