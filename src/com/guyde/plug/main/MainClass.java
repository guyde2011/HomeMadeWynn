package com.guyde.plug.main;

import java.io.File;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.guyde.plug.data.Archer;
import com.guyde.plug.data.Assassin;
import com.guyde.plug.data.PlayerDataManager;
import com.guyde.plug.data.PlayerHealthRegen;
import com.guyde.plug.data.PlayerManaRegen;
import com.guyde.plug.data.QuestNPC;
import com.guyde.plug.data.Quests;
import com.guyde.plug.data.RegisterAggro;
import com.guyde.plug.data.RegisterArmor;
import com.guyde.plug.data.RegisterMobRegion;
import com.guyde.plug.data.RegisterQuest;
import com.guyde.plug.data.RegisterQuestNPC;
import com.guyde.plug.data.RegisterWeapon;
import com.guyde.plug.data.WynnItems;
import com.guyde.plug.data.WynnMobs;


public class MainClass extends JavaPlugin{
    @Override
    public void onEnable() {
    	getServer().getPluginManager().registerEvents(new GuydeEventHandler(), this);
    	instance = this;
    	world = Bukkit.getWorlds().get(0);
    	new PlayerHealthRegen().run();
    	new PlayerManaRegen().run();
    }
    
    @Override
    public void onDisable() {

    }
    
    public static World world;
    public static MainClass instance;
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
			} else {
				sender.sendMessage("Could not find the mobs directory");
				return true;
			}
			File weapons1 = new File(new File(".").getAbsolutePath() + "/mob-regions");
			if (weapons1.exists() && weapons1.isDirectory()){
				for (File cur : weapons1.listFiles()){
					glob.set("register", CoerceJavaToLua.coerce(new RegisterMobRegion()));
					LuaValue c = glob.loadfile(cur.getAbsolutePath());
					c.call();
				}
				sender.sendMessage("Successfully loaded all mob regions");
				return true;
			}
			sender.sendMessage("Could not find the mob-regions directory");
			return true;
		}
		if (cmd.getName().toLowerCase().equals("readquests")){
			for (Entity ent : Quests.kill()){
				ent.remove();
			}
			Quests.reset();
			Globals glob = JsePlatform.standardGlobals();
			File weapons = new File(new File(".").getAbsolutePath() + "/quests/npc");
			if (weapons.exists() && weapons.isDirectory()){
				for (File cur : weapons.listFiles()){
					glob.set("register", CoerceJavaToLua.coerce(new RegisterQuestNPC()));
					LuaValue c = glob.loadfile(cur.getAbsolutePath());
					c.call();
				}
				sender.sendMessage("Successfully loaded all Quest NPCs");
			} else {
		       sender.sendMessage("Could not find the Quest NPCs directory");
		       return true;
			}
			File weapons1 = new File(new File(".").getAbsolutePath() + "/quests/quest");
			if (weapons1.exists() && weapons1.isDirectory()){
				for (File cur : weapons1.listFiles()){
					glob.set("register", CoerceJavaToLua.coerce(new RegisterQuest()));
					LuaValue c = glob.loadfile(cur.getAbsolutePath());
					c.call();
				}
				sender.sendMessage("Successfully loaded all Quests");
			} else {
		       sender.sendMessage("Could not find the Quests directory");
		       return true;
			}
			for (Entry<String,QuestNPC> npc : Quests.getNPCs().entrySet()){
				npc.getValue().Spawn(((Entity)sender).getWorld());
			}
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
		if (cmd.getName().toLowerCase().equals("class")){
			if (args[0].equals("Archer")){
				PlayerDataManager.setClass((Player)sender,new Archer(((Player)sender).getUniqueId()));
			} else if (args[0].equals("Assassin")){
				PlayerDataManager.setClass((Player)sender,new Assassin(((Player)sender).getUniqueId()));
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

