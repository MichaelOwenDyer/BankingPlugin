package com.monst.bankingplugin.configuration;

public abstract class ConfigurationNode {
    
    final String key;
    
    public ConfigurationNode(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
    
    protected abstract void populate(Object object);
    
    protected abstract Object getAsYaml();

}
