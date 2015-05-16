package com.guyde.plug.utils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class MCFormat {
	public static final MCFormat CLEAN = new MCFormat("");
	public static final MCFormat NEW_LINE = new MCFormat("\n");
	public static final MCFormat RED = new MCFormat(ChatColor.RED + "");
	public static final MCFormat GREEN = new MCFormat(ChatColor.GREEN + "");
	public static final MCFormat DARK_RED = new MCFormat(ChatColor.DARK_RED + "");
	public static final MCFormat DARK_GREEN = new MCFormat(ChatColor.DARK_GREEN + "");
	public static final MCFormat AQUA = new MCFormat(ChatColor.AQUA + "");
	public static final MCFormat BLUE = new MCFormat(ChatColor.BLUE + "");
	public static final MCFormat DARK_AQUA = new MCFormat(ChatColor.DARK_AQUA + "");
	public static final MCFormat DARK_BLUE = new MCFormat(ChatColor.DARK_BLUE + "");
	public static final MCFormat GRAY = new MCFormat(ChatColor.GRAY + "");
	public static final MCFormat PURPLE = new MCFormat(ChatColor.LIGHT_PURPLE + "");
	public static final MCFormat DARK_GRAY = new MCFormat(ChatColor.DARK_GRAY + "");
	public static final MCFormat DARK_PURPLE = new MCFormat(ChatColor.DARK_PURPLE+ "");
	public static final MCFormat BLACK = new MCFormat(ChatColor.BLACK + "");
	public static final MCFormat WHITE = new MCFormat(ChatColor.WHITE + "");
	public static final MCFormat YELLOW = new MCFormat(ChatColor.YELLOW + "");
	public static final MCFormat GOLD = new MCFormat(ChatColor.GOLD + "");
	public static final MCFormat RANDOM = new MCFormat(ChatColor.MAGIC + "");
	public static final MCFormat CLEAR = new MCFormat(ChatColor.RESET + "");
	public static final MCFormat BOLD = new MCFormat(ChatColor.BOLD + "");
	public static final MCFormat UNDERLINE = new MCFormat(ChatColor.UNDERLINE + "");
	public static final MCFormat STRIKETHROUGH = new MCFormat(ChatColor.STRIKETHROUGH + "");
	public static final MCFormat ITALIC = new MCFormat(ChatColor.ITALIC + "");
	public static final LuaValue MC_FORMAT; 
	
	static {
		MC_FORMAT = new LuaTable();
		MC_FORMAT.set("CLEAN", CLEAN.str);
		MC_FORMAT.set("NEW_LINE", NEW_LINE.str);
		MC_FORMAT.set("RED", RED.str);
		MC_FORMAT.set("DARK_RED", DARK_RED.str);
		MC_FORMAT.set("GREEN", GREEN.str);
		MC_FORMAT.set("DARK_GREEN", DARK_GREEN.str);
		MC_FORMAT.set("AQUA", AQUA.str);
		MC_FORMAT.set("DARK_AQUA", DARK_AQUA.str);
		MC_FORMAT.set("BLUE", BLUE.str);
		MC_FORMAT.set("DARK_BLUE", DARK_BLUE.str);
		MC_FORMAT.set("PURPLE", PURPLE.str);
		MC_FORMAT.set("DARK_PURPLE", DARK_PURPLE.str);
		MC_FORMAT.set("GRAY", GRAY.str);
		MC_FORMAT.set("DARK_GRAY", DARK_GRAY.str);
		MC_FORMAT.set("GOLD", GOLD.str);
		MC_FORMAT.set("BLACK", BLACK.str);
		MC_FORMAT.set("WHITE", WHITE.str);
		MC_FORMAT.set("YELLOW", YELLOW.str);
		MC_FORMAT.set("RANDOM", RANDOM.str);
		MC_FORMAT.set("CLEAR", CLEAR.str);
		MC_FORMAT.set("BOLD", BOLD.str);
		MC_FORMAT.set("UNDERLINE", UNDERLINE.str);
		MC_FORMAT.set("STRIKETHROUGH", STRIKETHROUGH.str);
		MC_FORMAT.set("ITALIC", ITALIC.str);
	}
	private String str;
	
	
	public final void append(MCFormat format){ 
		str = str + format;
	}
	
	public final void append(String format){ 
		str = str + format;
	}
	
	public final boolean HasNewLine(){ 
		return str.contains("\\n");
	}

	private MCFormat(String format){
		str = format;
		
	}
	
	public static MCFormat format(String format){
		return new MCFormat(format);
	}
	
	public List<String> seperateLines(){
		return Arrays.asList(str.split("\\n"));
	}
	
	

}
