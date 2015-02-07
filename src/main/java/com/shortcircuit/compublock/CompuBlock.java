package com.shortcircuit.compublock;

import com.shortcircuit.compublock.listeners.CompuListener;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author ShortCircuit908
 */
public class CompuBlock extends JavaPlugin{
	public void onEnable(){
		getServer().getPluginManager().registerEvents(new CompuListener(this), this);
		saveDefaultConfig();
	}
}
