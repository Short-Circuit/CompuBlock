package com.shortcircuit.compublock.listeners;

import com.shortcircuit.compublock.CompuBlock;
import com.shortcircuit.compublock.Metadater;
import com.shortcircuit.compublock.consoles.ComputerBlock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @author ShortCircuit908
 */
public class CompuListener implements Listener {
	private CompuBlock plugin;

	public CompuListener(CompuBlock plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void transferScript(final PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Block block = event.getClickedBlock();
		if(event.isCancelled()
				|| item == null
				|| (!item.getType().equals(Material.BOOK_AND_QUILL)
				&& !item.getType().equals(Material.WRITTEN_BOOK))
				|| block == null
				|| !block.getType().equals(Material.COMMAND)
				|| !event.getPlayer().hasPermission("lua")) {
			return;
		}
		CommandBlock command_block = (CommandBlock)block.getState();
		command_block.setCommand(loadFromBook((BookMeta)item.getItemMeta()));
		command_block.update(true);
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void runBlock(final BlockRedstoneEvent event) {
		if(event.getNewCurrent() == event.getOldCurrent() || event.getNewCurrent() == 0 || event.getBlock() == null
				|| !event.getBlock().getType().equals(Material.COMMAND)) {
			return;
		}
		Block command_block = event.getBlock();
		CommandBlock block = (CommandBlock)command_block.getState();
		ComputerBlock computer;
		if(command_block.hasMetadata("computer")) {
			try {
				computer = (ComputerBlock)command_block.getMetadata("computer").get(0).value();
			}
			catch(ClassCastException e){
				computer = new ComputerBlock(plugin, block);
				command_block.setMetadata("computer", new Metadater(plugin, computer));
			}
		}
		else {
			computer = new ComputerBlock(plugin, block);
			command_block.setMetadata("computer", new Metadater(plugin, computer));
		}
		computer.run();
	}

	private String loadFromBook(BookMeta book) {
		String lua_string = "";
		for(String page : book.getPages()) {
			lua_string += page.replace("\n", "; ") + "; ";
		}
		return lua_string;
	}
}
