package com.guyde.plug.main;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.guyde.plug.data.Archer;
import com.guyde.plug.data.Assassin;
import com.guyde.plug.data.PlayerDataManager;
import com.guyde.plug.data.PlayerHealthRegen;
import com.guyde.plug.data.PlayerManaRegen;
import com.guyde.plug.data.Quests;
import com.guyde.plug.data.RegisterAggro;
import com.guyde.plug.data.RegisterArmor;
import com.guyde.plug.data.RegisterMobRegion;
import com.guyde.plug.data.RegisterNPC;
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
    	reload();
    	new PlayerHealthRegen().run();
    	new PlayerManaRegen().run();
    }
    
    public final Chatter info = new Chatter("§3[§bINFO§3]§b ");
    public final Chatter info_err = new Chatter("§4[§cINFO§4]§c ");
    @Override
    public void onDisable() {

    }
    	

    public void readNPCs(){
    	Globals glob = JsePlatform.standardGlobals();
    	File npcs = new File(new File(".").getAbsolutePath() + "/npc");
    	if (npcs.exists() && npcs.isDirectory()){
    		for (File cur : npcs.listFiles()){
    			glob.set("register", CoerceJavaToLua.coerce(new RegisterNPC()));
    			try{
    				LuaValue c = glob.loadfile(cur.getAbsolutePath());
        			c.call();
    	    	} catch (LuaError e){
    	    		info_err.broadcast("Lua Error at file" + cur.getAbsolutePath() + ": " + e.getLocalizedMessage());
    	    	}
    		}
    		info.broadcast("Successfully loaded all valid NPC files");
    		return;
    	}
    	info_err.broadcast("Could not find the NPCs directory");
  
    }
    
    public void readWeapons(){
    	Globals glob = JsePlatform.standardGlobals();
    	File weapons = new File(new File(".").getAbsolutePath() + "/weapons");
    	if (weapons.exists() && weapons.isDirectory()){
    		for (File cur : weapons.listFiles()){
    			glob.set("register", CoerceJavaToLua.coerce(new RegisterWeapon()));
    			try{
    				LuaValue c = glob.loadfile(cur.getAbsolutePath());
        			c.call();
    	    	} catch (LuaError e){
    	    		info_err.broadcast("Lua Error at file" + cur.getAbsolutePath() + ": " + e.getLocalizedMessage());
    	    	}
    		}
    		info.broadcast("Successfully loaded all valid weapon files");
    		return;
    	}
    	info_err.broadcast("Could not find the weapons directory");
  
    }
    
    public void readArmors(){
    	Globals glob = JsePlatform.standardGlobals();
    	File armor = new File(new File(".").getAbsolutePath() + "/armors");
    	if (armor.exists() && armor.isDirectory()){
    		for (File cur : armor.listFiles()){
    			glob.set("register", CoerceJavaToLua.coerce(new RegisterArmor()));
    			try{
    				LuaValue c = glob.loadfile(cur.getAbsolutePath());
        			c.call();
    	    	} catch (LuaError e){
    	    		info_err.broadcast("Lua Error at file" + cur.getAbsolutePath() + ": " + e.getLocalizedMessage());
    	    	}
    		}
    		info.broadcast("Successfully loaded all armor files");
    		return;
    	}
    	info_err.broadcast("Could not find the armors directory");
    }
    
    public void readMobs(){
    	Globals glob = JsePlatform.standardGlobals();
    	File mob = new File(new File(".").getAbsolutePath() + "/mobs");
    	if (mob.exists() && mob.isDirectory()){
    		for (File cur : mob.listFiles()){
    			glob.set("register", CoerceJavaToLua.coerce(new RegisterAggro()));
    			try{
    				LuaValue c = glob.loadfile(cur.getAbsolutePath());
        			c.call();
    	    	} catch (LuaError e){
    	    		info_err.broadcast("Lua Error at file" + cur.getAbsolutePath() + ": " + e.getLocalizedMessage());
    	    	}
    		}
    		info.broadcast("Successfully loaded all mob files");
    		return;
    	}
    	info_err.broadcast("Could not find the mobs directory");
    }
    
    public void readMobRegions(){
    	Globals glob = JsePlatform.standardGlobals();
    	File mob_regions = new File(new File(".").getAbsolutePath() + "/mob-regions");
    	if (mob_regions.exists() && mob_regions.isDirectory()){
    		for (File cur : mob_regions.listFiles()){
    			glob.set("register", CoerceJavaToLua.coerce(new RegisterMobRegion()));
    			try{
    				LuaValue c = glob.loadfile(cur.getAbsolutePath());
        			c.call();
    	    	} catch (LuaError e){
    	    		info_err.broadcast("Lua Error at file" + cur.getAbsolutePath() + ": " + e.getLocalizedMessage());
    	    	}
    		}
    		info.broadcast("Successfully loaded all mob regions files");
    		return;
    	}
    	info_err.broadcast("Could not find the mob regions directory");
    }
    
    public void readQuestNPCs(){
    	for (Entity ent : Quests.kill()){
			ent.remove();
		}
    	Quests.kill_$eq(new Entity[]{});
		Globals glob = JsePlatform.standardGlobals();
		File quest_npcs = new File(new File(".").getAbsolutePath() + "/quests/npc");
		if (quest_npcs.exists() && quest_npcs.isDirectory()){
			for (File cur : quest_npcs.listFiles()){
				glob.set("register", CoerceJavaToLua.coerce(new RegisterQuestNPC()));
				try{
    				LuaValue c = glob.loadfile(cur.getAbsolutePath());
        			c.call();
    	    	} catch (LuaError e){
    	    		info_err.broadcast("Lua Error at file" + cur.getAbsolutePath() + ": " + e.getLocalizedMessage());
    	    	}
    		}
    		info.broadcast("Successfully loaded all quest NPCs");
    		return;
    	}
    	info_err.broadcast("Could not find the quest NPCs directory");
    }
    
    public void readQuests(){
		Globals glob = JsePlatform.standardGlobals();
		File quests = new File(new File(".").getAbsolutePath() + "/quests/quest");
		if (quests.exists() && quests.isDirectory()){
			for (File cur : quests.listFiles()){
				glob.set("register", CoerceJavaToLua.coerce(new RegisterQuest()));
				try{
    				LuaValue c = glob.loadfile(cur.getAbsolutePath());
        			c.call();
    	    	} catch (LuaError e){
    	    		info_err.broadcast("Lua Error at file" + cur.getAbsolutePath() + ": " + e.getLocalizedMessage());
    	    	}
    		}
    		info.broadcast("Successfully loaded all quest files");
    		return;
    	}
    	info_err.broadcast("Could not find the quests directory");
    }
    
    public void killMobs(){
    	for (LivingEntity ent : world.getLivingEntities()){
    		if (!(ent instanceof Player)) ent.remove();
    	}    	
    	for (Entity ent : world.getEntities()){
    		if (ent instanceof ArmorStand) ent.remove();
    	}
    }
    
    public void reload(){
    	world = Bukkit.getWorlds().get(0);
    	killMobs();
    	readArmors();
    	readWeapons();
    	readQuestNPCs();
    	readNPCs();
    	readQuests();
    	readMobs();
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
		if (cmd.getName().toLowerCase().equals("readlua")){
			info.broadcast("§2[§a" + sender.getName() + "§2]§b Has issued a Lua reload" );
			reload();
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

		return false;
	}
	

}

