package com.guyde.plug.main;



import java.util.List;
import java.util.Random;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.guyde.plug.data.Assassin;
import com.guyde.plug.data.BowCooldown;
import com.guyde.plug.data.Clicks;
import com.guyde.plug.data.DamageManager;
import com.guyde.plug.data.DropRate;
import com.guyde.plug.data.DropRates;
import com.guyde.plug.data.IdentifiesHelper;
import com.guyde.plug.data.PlayerDataManager;
import com.guyde.plug.utils.CustomNameTracker;
import com.guyde.plug.utils.TextCreator;


public class GuydeEventHandler implements Listener{

	@EventHandler(priority=EventPriority.LOW)
	public void playerChat(AsyncPlayerChatEvent event){

		
		String message = ChatColor.GRAY + "[" + PlayerDataManager.GetClass(event.getPlayer()).chatname() + "/" + event.getPlayer().getLevel() + "] " + ChatColor.WHITE + event.getPlayer().getDisplayName() + ": " + event.getMessage();
	    Bukkit.broadcastMessage(message);
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event){
		LivingEntity entity = event.getEntity();
		event.setDroppedExp(0);
		List<ItemStack> drops = event.getDrops();
		drops.clear();
		if (!entity.hasMetadata("mob_level")){
			return;
		}
		int xp = -1;
		int loot = 0;
		Player killer = null;
		if (event.getEntity().getLastDamageCause().getEntity()!=null){
			if (event.getEntity().getLastDamageCause().getEntity() instanceof Arrow){
				ProjectileSource src = ((Arrow)event.getEntity().getLastDamageCause().getEntity()).getShooter();
				if (src instanceof Player){
					loot = IdentifiesHelper.getLootBonus((Player)src);
					killer = (Player)src;
					if (event.getEntity().hasMetadata("min_exp")){
						xp = IdentifiesHelper.getXP((Player)src, event.getEntity().getMetadata("min_exp").get(0).asInt(), event.getEntity().getMetadata("max_exp").get(0).asInt());
					}
				}
			} else if (event.getEntity().getLastDamageCause().getEntity() instanceof Player){
				loot = IdentifiesHelper.getLootBonus(event.getEntity().getKiller());
				killer = event.getEntity().getKiller();
				if (event.getEntity().hasMetadata("min_exp")){
					xp = IdentifiesHelper.getXP(event.getEntity().getKiller(), event.getEntity().getMetadata("min_exp").get(0).asInt(), event.getEntity().getMetadata("max_exp").get(0).asInt());
				}
			}
			
		}
		if (event.getEntity().getKiller()!=null){
			loot = IdentifiesHelper.getLootBonus(event.getEntity().getKiller());
			killer = event.getEntity().getKiller();
			if (event.getEntity().hasMetadata("min_exp")){
				xp = IdentifiesHelper.getXP(event.getEntity().getKiller(), event.getEntity().getMetadata("min_exp").get(0).asInt(), event.getEntity().getMetadata("max_exp").get(0).asInt());
			}
					
		}
		int i = entity.getMetadata("mob_level").get(0).asInt();
		for (DropRate drop : DropRates.getDropRateRange(i)){
			if (new Random().nextInt((i+45)*(5+i) - loot)<drop.rate()){
				drops.add(drop.item().CreateItemStack());
			}
		}
		if (xp!=-1){
			TextCreator.createTextAt(ChatColor.GRAY + "[" + ChatColor.RESET + "+" + xp + "XP" + ChatColor.GRAY + "]", event.getEntity().getEyeLocation().add(0,0.5,0), 60);
			TextCreator.createTextAt(ChatColor.GRAY + "[" + killer.getDisplayName() + "]", event.getEntity().getEyeLocation().add(0,0.25,0), 60);
		}
		
	}
	
	

	@EventHandler
	public void entityDamageEntity(EntityDamageByEntityEvent event){
		if (event.getEntity().getType()==EntityType.ARMOR_STAND){
			if (event.getEntity().hasMetadata("tracking")){
				((LivingEntity)event.getEntity().getMetadata("tracking").get(0).value()).damage(event.getDamage(), event.getDamager());
			}
			event.setCancelled(true); return;
		}
		Entity damager = event.getDamager();
		Entity damaged = event.getEntity();
		double damage = event.getDamage();
		int[] defense = new int[8];
		if (damager instanceof Player && ((Player)damager).getItemInHand()!=null && ((Player)damager).getItemInHand().getType()!=Material.BOW){
			damage = DamageManager.getDamageFor((Player)damager);
			Player p = (Player)damager;
			p.setFoodLevel(Math.min(20,p.getFoodLevel() + IdentifiesHelper.ManaPerHit(p)));
			p.setHealth(Math.min(20,p.getHealth() + IdentifiesHelper.getIDFor(p,"hstl")/10d));
		} else if (damager instanceof Arrow){
			if (((Arrow)damager).getShooter() instanceof Player){
				damage = DamageManager.getDamageFor((Player)((Arrow)damager).getShooter());
				if (((Arrow)damager).hasMetadata("dmg_multi")){
					damage = damage * (float)((Arrow)damager).getMetadata("dmg_multi").get(0).value();
				}
				Player p =(Player)((Arrow)damager).getShooter();
				p.setFoodLevel(Math.min(20,p.getFoodLevel() + IdentifiesHelper.ManaPerHit(p)));
				p.setHealth(Math.min(20,p.getHealth() + IdentifiesHelper.getIDFor(p,"hstl")/10d));
			}
		} else if (damager.hasMetadata("min_dmg")){
			damage = new Random().nextInt(damager.getMetadata("max_dmg").get(0).asInt()-damager.getMetadata("min_dmg").get(0).asInt())+damager.getMetadata("min_dmg").get(0).asInt();
		}
		
		if (damaged instanceof Player){
			defense = DamageManager.getDefenseFor((Player)damaged);
			event.setDamage(DamageManager.damageAgainst(damage, defense));
		} else if (!damaged.hasMetadata("tracked")){
			event.setDamage(damage);
			LivingEntity ent = (LivingEntity)damaged;
			int place = (int)Math.ceil((10 * (Math.max(0,ent.getHealth()-event.getDamage())))/ent.getMaxHealth());
			String str = ChatColor.DARK_RED + "[" + ChatColor.RED;
			ChatColor cur = ChatColor.RED;
			for (int k = 0; k<10; k++){
				if (k==5){
					str = str + ChatColor.DARK_RED + (int)Math.max(0,ent.getHealth()-event.getDamage()) + cur;
				}
				if (k==place){
					str = str + ChatColor.GRAY;
					cur = ChatColor.GRAY;
				}
				
				str = str + "|";
			}
			str = str + ChatColor.DARK_RED + "]";
			CustomNameTracker tracker = TextCreator.createTrackedText(str , ent, new Vector(0,-0.125,0));
			ent.setMetadata("tracked", new FixedMetadataValue(MainClass.instance,tracker));
		} else {
			event.setDamage(damage);
			LivingEntity ent = (LivingEntity)damaged;
			CustomNameTracker tracker = (CustomNameTracker)ent.getMetadata("tracked").get(0).value();
			int place = (int)Math.ceil((10 * (Math.max(0,ent.getHealth()-event.getDamage())))/ent.getMaxHealth());
			String str = ChatColor.DARK_RED + "[" + ChatColor.RED;
			ChatColor cur = ChatColor.RED;
			for (int k = 0; k<10; k++){
				if (k==5){
					str = str + ChatColor.DARK_RED + (int)Math.max(0,ent.getHealth()-event.getDamage()) + cur;
				}
				if (k==place){
					str = str + ChatColor.GRAY;
					cur = ChatColor.GRAY;
				}
				
				str = str + "|";
			}
			str = str + ChatColor.DARK_RED + "]";
			tracker.updateName(str);
			ent.setMetadata("tracked", new FixedMetadataValue(MainClass.instance,tracker));
		}
			
	}
	
	@EventHandler
	public void onPlayerEntityClick(org.bukkit.event.player.PlayerInteractEntityEvent event){
	}
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event){
		if (event.getPlayer().getItemInHand()==null || event.getPlayer().getItemInHand().getType()==Material.AIR){
			PlayerDataManager.setClass(event.getPlayer(), new Assassin(event.getPlayer().getUniqueId()));
		}
		if (event.getAction()==Action.LEFT_CLICK_AIR || event.getAction()==Action.LEFT_CLICK_BLOCK){
			if (event.getPlayer().getItemInHand()!=null && event.getPlayer().getItemInHand().getType()!=Material.AIR && event.getPlayer().getItemInHand().getType()==PlayerDataManager.GetClass(event.getPlayer()).getWeapon()){
				PlayerDataManager.Click(event.getPlayer(), Clicks.Left());	
				if (PlayerDataManager.GetClass(event.getPlayer()).name().equals("Assassin")){
					event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
				}
			}
		}
		if (event.getAction()==Action.RIGHT_CLICK_AIR || event.getAction()==Action.RIGHT_CLICK_BLOCK){
			if (event.getPlayer().getItemInHand()!=null && event.getPlayer().getItemInHand().getType()==PlayerDataManager.GetClass(event.getPlayer()).getWeapon()){
				PlayerDataManager.Click(event.getPlayer(), Clicks.Right());	
				if (PlayerDataManager.GetClass(event.getPlayer()).name().equals("Archer") && event.getPlayer().getItemInHand()!=null && event.getPlayer().getItemInHand().getType()==Material.BOW){
					event.setCancelled(true);
					ItemStack copy = event.getPlayer().getItemInHand().clone();
					event.getPlayer().setItemInHand(null);
					event.getPlayer().setItemInHand(copy);
					if (!event.getPlayer().hasMetadata("bow_charge")){
						Arrow arrow = event.getPlayer().launchProjectile(Arrow.class);
						arrow.setShooter(event.getPlayer());
						event.getPlayer().setMetadata("bow_charge", new FixedMetadataValue(MainClass.instance , true));
						new BowCooldown(event.getPlayer()).runTaskLater(MainClass.instance, 10);
					}
				}
			}
		}
		
		
	}
	
	
}
