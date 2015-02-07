package com.shortcircuit.compublock.lua.reflect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shortcircuit.compublock.CompuBlock;
import com.shortcircuit.compublock.lua.LuaGlobals;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ShortCircuit908
 */
public class ClassWrapper<T> extends TwoArgFunction {
	private final Gson gson = new GsonBuilder().serializeNulls().create();
	private Globals globals;
	private LuaValue mod_name, environment;
	private final T value;
	private final Class<? extends T> class_of_T;
	private final CompuBlock plugin;

	public ClassWrapper(CompuBlock plugin, T value, Class<? extends T> class_of_T) {
		this.plugin = plugin;
		this.value = value;
		this.class_of_T = class_of_T;
	}

	@Override
	public LuaValue call(final LuaValue mod_name, final LuaValue environment) {
		this.mod_name = mod_name;
		this.environment = environment;
		globals = environment.checkglobals();
		LuaTable table = new LuaTable();
		if(plugin.getConfig().getStringList("Blacklist.Classes").contains(class_of_T.getName())){
			return table;
		}
		for(String method_name : getMethodNames()) {
			if(!plugin.getConfig().getStringList("Blacklist.Methods").contains(class_of_T.getName() + "." + method_name)) {
				table.set(method_name, new VarArgFunction() {
					private Object target;
					private List<Method> methods;
					public VarArgFunction setData(Object target, List<Method> methods) {
						this.target = target;
						this.methods = methods;
						return this;
					}

					@Override
					public Varargs invoke(Varargs args) {
						try {
							Method method = getMatchingMethod(methods, args.narg());
							if(method.getReturnType() != void.class) {
								Object returned = method.invoke(target, convertVarargs(args, method.getGenericParameterTypes()));
								if(returned instanceof Integer) {
									return LuaValue.valueOf((Integer)returned);
								}
								if(returned instanceof Double) {
									return LuaValue.valueOf((Double)returned);
								}
								if(returned instanceof Boolean) {
									return LuaValue.valueOf((Boolean)returned);
								}
								if(returned instanceof String) {
									return LuaValue.valueOf((String)returned);
								}
								if(returned.getClass().isArray()){

								}
								LuaValue parse_return = new ClassWrapper<>(plugin, returned, returned.getClass());
								LuaGlobals.load(globals, parse_return);
								return parse_return.call(mod_name, environment);
							}
							else {
								method.invoke(target, convertVarargs(args, method.getGenericParameterTypes()));
								return LuaValue.NIL;
							}
						}
						catch(Exception e) {
							throw new LuaError(e);
						}
					}
				}.setData(value, getMatchingMethods(method_name)));
			}
		}
		table.set("newInstance", new newInstance());
		environment.set(class_of_T.getSimpleName(), table);
		return table;
	}

	class newInstance extends VarArgFunction {
		public LuaValue invoke(Varargs args) {
			try {
				T new_instance = null;
				for(Constructor constructor : class_of_T.getDeclaredConstructors()) {
					constructor.setAccessible(true);
					if(constructor.getGenericParameterTypes().length == args.narg()) {
						List<Object> args_list = new ArrayList<>();
						for(int i = 1; i < args.narg() + 1; i++) {
							LuaValue arg = args.arg(i);
							Type arg_type = constructor.getGenericParameterTypes()[i - 1];
							Object parsed_arg = gson.fromJson(arg.tojstring(), arg_type);
							args_list.add(parsed_arg);
						}
						new_instance = (T)constructor.newInstance(args_list.toArray());
					}
				}
				return new ClassWrapper<>(plugin, new_instance, class_of_T).call(mod_name, environment);
			}
			catch(Exception e) {
				throw new LuaError(e);
			}
		}
	}

	private List<String> getMethodNames() {
		List<String> names = new ArrayList<>();
		for(Method method : class_of_T.getDeclaredMethods()) {
			if(!names.contains(method.getName())) {
				names.add(method.getName());
			}
		}
		return names;
	}

	private List<Method> getMatchingMethods(String name) {
		List<Method> methods = new ArrayList<>();
		for(Method method : class_of_T.getDeclaredMethods()) {
			if(method.getName().equals(name)) {
				methods.add(method);
			}
		}
		return methods;
	}

	private Method getMatchingMethod(List<Method> methods, int arg_length) {
		Method match = null;
		for(Method method : methods) {
			int param_size = method.getParameterTypes().length;
			if(param_size == arg_length) {
				return method;
			}
			if(param_size > 0 && method.getParameterTypes()[param_size - 1].isArray() && arg_length >= param_size) {
				match = method;
			}
		}
		return match;
	}

	private Object[] convertVarargs(Varargs args, Type[] parameter_types) {
		List<Object> varargs = new ArrayList<>();
		for(int i = 1; i < args.narg() + 1; i++) {
			LuaValue arg = args.arg(i);
			Object parsed = gson.fromJson(arg.tojstring(), parameter_types[i - 1]);
			varargs.add(parsed);
		}
		return varargs.toArray();
	}
}
