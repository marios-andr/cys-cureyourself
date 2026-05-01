package io.github.marios_andr.cys.config;

import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ConfigProvider implements DefaultConfig {

    private String configContent = "";
    private final List<Pair> configList = new ArrayList<>();

    public ConfigProvider() {

    }

    @Override
    public String get(String namespace) {
        return this.configContent;
    }

    public <T> T addEntry(String name, T obj, String comment) {
        Pair<String, T> pair = Pair.of(name, obj);
        this.configList.add(pair);
        configContent += pair.getFirst() + " = " + pair.getSecond() + " #" + comment + " | default: " + pair.getSecond() + "\n";
        return pair.getSecond();
    }

    public void addComment(String comment) {
        configContent += "# " + comment + "\n";
    }
}
