module jpa99 {
    requires transitive java.persistence;
    requires transitive org.jinq.api;
    requires transitive org.jinq.jpa;
    requires io.github.classgraph;
    exports io.github.ossnass.jpa99;
}