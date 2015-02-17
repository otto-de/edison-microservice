package de.otto.µservice.testsupport.dsl;

public class Given {
    public static final Given INSTANCE = new Given();

    public static void given(final Given... givenStuff) {}

    public static Given and(final Given givenStuff, final Given... moreGivenStuff) { return INSTANCE; }

}
