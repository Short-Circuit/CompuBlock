package com.shortcircuit.compublock.lua;

import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.shortcircuit.compublock.CompuBlock;
import com.shortcircuit.compublock.consoles.ComputerBlock;
import com.shortcircuit.compublock.lua.reflect.ClassWrapper;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.Random;
import java.util.UUID;

/**
 * @author ShortCircuit908
 */
public class LuaGlobals {
	private final Globals globals;

	public static LuaValue load(Globals globals, LuaValue library) {
		return globals.load(library);
	}

	public LuaGlobals(final CompuBlock plugin, final ComputerBlock context) {
		globals = JsePlatform.debugGlobals();
		globals.load(new ClassWrapper<EnchantmentWrapper>(plugin, null, EnchantmentWrapper.class));
		globals.load(new ClassWrapper<Bukkit>(plugin, null, Bukkit.class));
		globals.load(new ClassWrapper<UUID>(plugin, null, UUID.class));
		globals.load(new ClassWrapper<Random>(plugin, new Random(System.currentTimeMillis()), Random.class));
		globals.set("print", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				for(int i = 1; i < args.narg() + 1; i++) {
					LuaValue value = args.arg(i);
					for(int line_num = 0; line_num <= context.line_pos; line_num++) {
						try {
							HologramLine line = context.hologram.getLine(context.line_pos);
							if(line_num == context.line_pos) {
								line.removeLine();
							}
						}
						catch(IndexOutOfBoundsException e) {
							context.getHologram().appendTextLine("");
						}
					}
					context.hologram.appendTextLine(value.tojstring());
					context.line_pos++;
				}
				return LuaValue.NIL;
			}
		});
		globals.set("clear", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				context.hologram.clearLines();
				context.line_pos = 0;
				return LuaValue.NIL;
			}
		});
		globals.set("setLinePos", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				context.line_pos = arg.toint();
				return LuaValue.NIL;
			}
		});
		globals.set("shutdown", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				//TODO: Halt script execution
				context.getHologram().delete();
				context.block.removeMetadata("computer", plugin);
				return LuaValue.NIL;
			}
		});
	}

	public Globals getGlobals() {
		return globals;
	}
}
