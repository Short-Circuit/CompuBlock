package com.shortcircuit.compublock.consoles;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.shortcircuit.compublock.CompuBlock;
import com.shortcircuit.compublock.lua.LuaGlobals;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

/**
 * @author ShortCircuit908
 */
public class ComputerBlock {
	private CompuBlock plugin;
	private CommandBlock command_block;
	public Block block;
	public Hologram hologram;
	public int line_pos = 1;
	private final Globals lua;

	public ComputerBlock(CompuBlock plugin, CommandBlock command_block) {
		this.plugin = plugin;
		this.command_block = command_block;
		this.block = command_block.getBlock();
		this.hologram = HologramsAPI.createHologram(plugin, block.getLocation().add(0.5, 3, 0.5));
		hologram.appendTextLine("CompuBlock 64");
		hologram.getVisibilityManager().setVisibleByDefault(true);
		hologram.setAllowPlaceholders(true);
		lua = new LuaGlobals(plugin, this).getGlobals();
	}

	public Hologram getHologram() {
		return hologram;
	}

	public int getLinePos() {
		return line_pos;
	}

	public void setLinePos(int line_pos) {
		this.line_pos = line_pos;
	}

	public void run() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				command_block = (CommandBlock)block.getState();
				try {
					LuaValue chunk = lua.load(command_block.getCommand());
					chunk.call();
				}
				catch(LuaError e) {
					if(plugin.getConfig().getBoolean("DebugToConsole")) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Lua error generated " +
								"by command block at " + block.getX() + ", " + block.getY() + ", " + block.getZ());
						e.printStackTrace();
					}
					hologram.appendTextLine(ChatColor.RED + e.getMessage().replace(command_block.getCommand(), ""));
					line_pos++;
				}
			}
		});
	}
}
