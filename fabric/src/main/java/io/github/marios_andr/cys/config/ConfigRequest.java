package io.github.marios_andr.cys.config;

import java.io.File;

public class ConfigRequest {
    final File file;
    final String filename;
    DefaultConfig provider;

    public ConfigRequest(File file, String filename ) {
        this.file = file;
        this.filename = filename;
        this.provider = DefaultConfig::empty;
    }

    /**
     * Sets the default config provider, used to generate the
     * config if it's missing.
     *
     * @param provider default config provider
     * @return current config request object
     * @see DefaultConfig
     */
    public ConfigRequest provider( DefaultConfig provider ) {
        this.provider = provider;
        return this;
    }

    /**
     * Loads the config from the filesystem.
     *
     * @return config object
     * @see BaseConfig
     */
    public BaseConfig request() {
        return new BaseConfig(this);
    }

    String getConfig() {
        return provider.get( filename ) + "\n";
    }
}
