package com.guyde.plug.main;



import java.util.List;
import java.util.Random;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R2.ChatComponentText;
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.IChatBaseComponent;
import net.minecraft.server.v1_8_R2.IMerchant;
import net.minecraft.server.v1_8_R2.MerchantRecipe;
import net.minecraft.server.v1_8_R2.MerchantRecipeList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.guyde.plug.data.BowCooldown;
import com.guyde.plug.data.Clicks;
import com.guyde.plug.data.DamageManager;
import com.guyde.plug.data.DropRate;
import com.guyde.plug.data.DropRates;
import com.guyde.plug.data.IdentifiesHelper;
import com.guyde.plug.data.MobDrop;
import com.guyde.plug.data.PlayerDataManager;
import com.guyde.plug.data.QuestBook;
import com.guyde.plug.data.QuestConstants;
import com.guyde.plug.data.Quests;
import com.guyde.plug.data.Trades;
import com.guyde.plug.utils.CustomNameTracker;
import com.guyde.plug.utils.ListenedProjectile;
import com.guyde.plug.utils.TextCreator;


public class GuydeEventHandler implements Listener{

	@EventHandler
	public void onPlayerLogOut(PlayerQuitEvent event){
		Player p = event.getPlayer();
		if (PlayerDataManager.GetClass(p)!=null){
			PlayerDataManager.GetClass(p).write(PlayerDataManager.getPlayerFile(p), PlayerDataManager.GetClass(p).name());
		}
	}
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
		if (entity.hasMetadata("drops")){
			for (MobDrop drop : (MobDrop[])entity.getMetadata("drops").get(0).value()){
				if (new Random().nextInt((i+45)*(5+i) - loot)<drop.rate()){
					drops.add(drop.getStack());
				}
			}
		}
		if (xp!=-1){
			TextCreator.createTextAt(ChatColor.GRAY + "[" + ChatColor.RESET + "+" + xp + "XP" + ChatColor.GRAY + "]", event.getEntity().getEyeLocation().add(0,0.5,0), 60);
			TextCreator.createTextAt(ChatColor.GRAY + "[" + killer.getDisplayName() + "]", event.getEntity().getEyeLocation().add(0,0.25,0), 60);
			PlayerDataManager.GetClass(killer).addEXP(xp);
		}
		
	}
	@EventHandler
	public void MobTarget(EntityTargetEvent event) {
		if (event.getTarget().getType()!=EntityType.PLAYER){
			event.setCancelled(true);
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
		if (event.getEntity().hasMetadata(QuestConstants.QUEST_NPC()) || event.getEntity().hasMetadata("merchant") ){
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
			if (damager instanceof LivingEntity){
				((Creature)damaged).setTarget((LivingEntity) damager);
			} else {
				((Creature)damaged).setTarget((LivingEntity)((Projectile)damager).getShooter());
			}
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
		if (event.getRightClicked().hasMetadata(QuestConstants.QUEST_NPC()) && event.getRightClicked().getMetadata(QuestConstants.QUEST_NPC()).get(0).asBoolean()){
			Quests.NpcClicked(event);
		}
		if (event.getRightClicked().hasMetadata("trades")){
			Trades trades = (Trades)event.getRightClicked().getMetadata("trades").get(0).value();
			MerchantRecipeList list = trades.getRecipeList();
			event.setCancelled(true);
			((CraftPlayer)event.getPlayer()).getHandle().openTrade(new IMerchant(){

				@Override
				public void a(MerchantRecipe arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void a_(EntityHuman arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void a_(net.minecraft.server.v1_8_R2.ItemStack arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public MerchantRecipeList getOffers(EntityHuman arg0) {
					// TODO Auto-generated method stub
					return list;
				}

				@Override
				public IChatBaseComponent getScoreboardDisplayName() {
					// TODO Auto-generated method stub
					return new ChatComponentText(event.getRightClicked().getMetadata("name").get(0).asString());
				}

				@Override
				public EntityHuman v_() {
					// TODO Auto-generated method stub
					return ((CraftPlayer)event.getPlayer()).getHandle();
				}
				
			});
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event){
		if(event.getEntity().hasMetadata("listened")){
			ListenedProjectile ent = (ListenedProjectile) event.getEntity().getMetadata("listened").get(0).value();
			ent.onHit();
			ent.proj().remove();
		}
	}
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event){
		if (event.getAction()==Action.LEFT_CLICK_AIR || event.getAction()==Action.LEFT_CLICK_BLOCK){
			if (event.getPlayer().getItemInHand()!=null && event.getPlayer().getItemInHand().getType()!=Material.AIR && event.getPlayer().getItemInHand().getType()==PlayerDataManager.GetClass(event.getPlayer()).getWeapon()){
				PlayerDataManager.Click(event.getPlayer(), Clicks.Left());	
				if (PlayerDataManager.GetClass(event.getPlayer()).name().equals("Assassin")){
					event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
				}
			}
		}
		if (event.getAction()==Action.RIGHT_CLICK_AIR || event.getAction()==Action.RIGHT_CLICK_BLOCK){
			if (event.getItem().getType()==Material.WRITTEN_BOOK){
				ItemStack stack = new QuestBook(event.getPlayer()).createBook();
				event.getPlayer().setItemInHand(stack);
				event.getPlayer().updateInventory();
				return;
			}
			if (event.getPlayer().getItemInHand()!=null && event.getPlayer().getItemInHand().getType()==PlayerDataManager.GetClass(event.getPlayer()).getWeapon()){
				PlayerDataManager.Click(event.getPlayer(), Clicks.Right());	
				if (PlayerDataManager.GetClass(event.getPlayer()).name().equals("Archer") && event.getPlayer().getItemInHand()!=null && event.getPlayer().getItemInHand().getType()==Material.BOW){
					event.setCancelled(true);
					ItemStack copy = event.getPlayer().getItemInHand().clone();
					event.getPlayer().setItemInHand(null);
					event.getPlayer().setItemInHand(copy);
					if (!event.getPlayer().hasMetadata("bow_charge")){
						Arrow arrow = event.getPlayer().getWorld().spawnArrow(event.getPlayer().getEyeLocation(),event.getPlayer().getEyeLocation().getDirection(),3,10);
						arrow.setShooter(event.getPlayer());
						event.getPlayer().setMetadata("bow_charge", new FixedMetadataValue(MainClass.instance , true));
						new BowCooldown(event.getPlayer()).runTaskLater(MainClass.instance, 10);
					}
				}
			}
		}
		
		
	}
	
	
}
