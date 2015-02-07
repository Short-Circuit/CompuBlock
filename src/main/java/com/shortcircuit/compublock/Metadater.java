package com.shortcircuit.compublock;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * @author ShortCircuit908
 *
 */
public class Metadater implements MetadataValue{
    private Plugin plugin;
    private Object value;
    public Metadater(Plugin plugin, Object value){
        this.plugin = plugin;
        this.value = value;
    }
    @Override
    public boolean asBoolean() throws ClassCastException{
        return (boolean)value;
    }
    @Override
    public byte asByte() throws ClassCastException{
        return (byte)value;
    }
    @Override
    public double asDouble() throws ClassCastException{
        return (double)value;
    }
    @Override
    public float asFloat() throws ClassCastException{
        return (float)value;
    }
    @Override
    public int asInt() throws ClassCastException{
        return (int)value;
    }
    @Override
    public long asLong() throws ClassCastException{
        return (long)value;
    }
    @Override
    public short asShort() throws ClassCastException{
        return (short)value;
    }
    @Override
    public String asString() throws ClassCastException{
        return (String)value;
    }
    @Override
    public Plugin getOwningPlugin() {
        return plugin;
    }
    @Override
    public void invalidate() {
        return;
    }
    @Override
    public Object value() {
        return value;
    }
}