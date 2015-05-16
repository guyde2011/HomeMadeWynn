package com.guyde.plug.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.guyde.plug.data.PlayerHealthRegen;
import com.guyde.plug.data.PlayerManaRegen;
import com.guyde.plug.data.RegisterAggro;
import com.guyde.plug.data.RegisterArmor;
import com.guyde.plug.data.RegisterWeapon;
import com.guyde.plug.data.WynnItems;
import com.guyde.plug.data.WynnMobs;


public class MainClass extends JavaPlugin{
    @Override
    public void onEnable() {
    	getServer().getPluginManager().registerEvents(new GuydeEventHandler(), this);
    	instance = this;
    	new PlayerHealthRegen().run();
    	new PlayerManaRegen().run();
    }
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }
    
    private static String readFile(File file) throws IOException{
    	BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
        
    }
    public static MainClass instance;
    static List<String> libs = new ArrayList<String>();
    static Map<String,String> pre = new HashMap<String,String>();
    static Map<String,String> chat = new HashMap<String,String>();
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().toLowerCase().equals("identify")){
			if (sender instanceof Player){
				Player player = ((Player)sender);
				ItemStack stack = player.getItemInHand();
				player.setItemInHand(WynnItems.IdentifyItem(stack));
				return true;
			}
		}
		if (cmd.getName().toLowerCase().equals("getweapon")){
			if (sender instanceof Player){
				String total = args[0];
				for (int i = 1; i<args.length; i++){
					total = total + " " + args[i];
				}
				((Player)sender).getInventory().addItem(WynnItems.getWeapon(total).CreateItemStack());
				return true;
			}
		}
		if (cmd.getName().toLowerCase().equals("readweapons")){
			Globals glob = JsePlatform.standardGlobals();
			File weapons = new File(new File(".").getAbsolutePath() + "/weapons");
			if (weapons.exists() && weapons.isDirectory()){
				for (File cur : weapons.listFiles()){
					glob.set("register", CoerceJavaToLua.coerce(new RegisterWeapon()));
					LuaValue c = glob.loadfile(cur.getAbsolutePath());
					c.call();
				}
				sender.sendMessage("Successfully loaded all weapons");
				return true;
			}
			sender.sendMessage("Could not find the weapons directory");
			return true;
		}
		
		if (cmd.getName().toLowerCase().equals("spawnmob")){
			if (sender instanceof Player){
				Player p = (Player)sender;
				for (int i = 0 ; i < Integer.parseInt(args[1]); i++){
					WynnMobs.getHostile(args[0]).createMob(p.getLocation());
				}
			}
		}
		if (cmd.getName().toLowerCase().equals("readmobs")){
			Globals glob = JsePlatform.standardGlobals();
			File weapons = new File(new File(".").getAbsolutePath() + "/mobs");
			if (weapons.exists() && weapons.isDirectory()){
				for (File cur : weapons.listFiles()){
					glob.set("register", CoerceJavaToLua.coerce(new RegisterAggro()));
					LuaValue c = glob.loadfile(cur.getAbsolutePath());
					c.call();
				}
				sender.sendMessage("Successfully loaded all mobs");
				return true;
			}
			sender.sendMessage("Could not find the mobs directory");
			return true;
		}
		if (cmd.getName().toLowerCase().equals("getarmor")){
			if (sender instanceof Player){
				String total = args[0];
				for (int i = 1; i<args.length; i++){
					total = total + " " + args[i];
				}
				((Player)sender).getInventory().addItem(WynnItems.getArmor(total).CreateItemStack());
				return true;
			}
		}
		if (cmd.getName().toLowerCase().equals("readarmors")){
			Globals glob = JsePlatform.standardGlobals();
			File weapons = new File(new File(".").getAbsolutePath() + "/armors");
			if (weapons.exists() && weapons.isDirectory()){
				for (File cur : weapons.listFiles()){
					glob.set("register", CoerceJavaToLua.coerce(new RegisterArmor()));
					LuaValue c = glob.loadfile(cur.getAbsolutePath());
					c.call();
				}
				sender.sendMessage("Successfully loaded all armors");
				return true;
			}
			sender.sendMessage("Could not find the armors directory");
			return true;
		}

		return false;
	}
	

}

