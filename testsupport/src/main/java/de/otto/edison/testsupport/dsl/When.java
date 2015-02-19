package de.otto.edison.testsupport.dsl;

public class When {
    public static final When INSTANCE = new When();

    public static void when(When... actions) {}
    public static When and(When actions, When... more) { return When.INSTANCE; }

}
