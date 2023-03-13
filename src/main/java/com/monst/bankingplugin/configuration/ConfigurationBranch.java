package com.monst.bankingplugin.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ConfigurationBranch extends ConfigurationNode {
    
    protected final Map<String, ConfigurationNode> children;
    
    public ConfigurationBranch(String key) {
        super(key);
        this.children = new LinkedHashMap<>();
    }
    
    public <T extends ConfigurationNode> T addChild(T child) {
        children.put(child.getKey(), child);
        return child;
    }
    
    public ConfigurationNode getChild(String key) {
        return children.get(key);
    }
    
    public Map<String, ConfigurationNode> getChildren() {
        return children;
    }
    
    @Override
    protected void populate(Object object) {
        Map<?, ?> map = object instanceof Map ? (Map<?, ?>) object : Collections.emptyMap();
        children.forEach((key, node) -> node.populate(map.get(key)));
    }
    
    @Override
    protected Object getAsYaml() {
        Map<String, Object> data = new LinkedHashMap<>();
        children.forEach((key, node) -> data.put(key, node.getAsYaml()));
        return data;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationBranch that = (ConfigurationBranch) o;
        return children.equals(that.children);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(key, children);
    }
    
}
