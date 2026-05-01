package io.github.marios_andr.cys.config;

public interface DefaultConfig {
    String get( String namespace );

    static String empty( String namespace ) {
        return "";
    }
}
