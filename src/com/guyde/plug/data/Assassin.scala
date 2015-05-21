package com.guyde.plug.data

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import com.guyde.plug.main.MainClass
import org.bukkit.entity.Arrow
import java.util.UUID
import java.util.Random
import org.bukkit.Material
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.Effect
import org.bukkit.util.Vector
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionEffect
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
class Assassin(uuid : UUID) extends GameClass(uuid,Clicks.Right,Material.SHEARS){
  var name = "Assassin"
  var chatname = "As"
  var level_requirement = 60
  object second_skill extends Skill("Vanish",4,3,2){
    def runSkill(level : Int , player : Player){
      player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,300,1))
      if (level>1){
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,300,2))
      }
      if (level==3){
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,300,2))
      }
    }
  } 
  
  class MultiHitter(player : Player , level : Int){
    var entities = DamageManager.damageNearbyWithout(player.getLocation.add(player.getEyeLocation.getDirection), 2, 2, 2, 0 ,player)
    for (i <- 1 to 10){
      new Hitter(player,0.25,entities).runTaskLater(MainClass.instance, i*3)
    }
    if (level > 1){
      new Hitter(player,1,entities).runTaskLater(MainClass.instance, 33)
    }
  }
  
  
  class Hitter(player : Player, multi : Double, entities : Array[Entity]) extends BukkitRunnable(){
    override def run(){
      entities.foreach { ent => 
        ent.getWorld.playEffect(ent.getLocation, Effect.TILE_BREAK, Material.PORTAL.getId) 
        ent.getWorld.playEffect(ent.getLocation.add(0,0.2,0), Effect.TILE_BREAK, Material.PORTAL.getId)
        ent.getWorld.playEffect(ent.getLocation.add(0.2,0,0), Effect.TILE_BREAK, Material.PORTAL.getId)
        ent.getWorld.playEffect(ent.getLocation.add(0,0,0.2), Effect.TILE_BREAK, Material.PORTAL.getId)
        ent.getWorld.playEffect(ent.getLocation, Effect.TILE_BREAK, Material.PORTAL.getId) 
        ent.getWorld.playEffect(ent.getLocation.add(0,-0.2,0), Effect.TILE_BREAK, Material.PORTAL.getId)
        ent.getWorld.playEffect(ent.getLocation.add(-0.2,0,0), Effect.TILE_BREAK, Material.PORTAL.getId)
        ent.getWorld.playEffect(ent.getLocation.add(0,0,-0.2), Effect.TILE_BREAK, Material.PORTAL.getId)
        ent.asInstanceOf[LivingEntity].damage(DamageManager.getDamageFor(player)*multi,player)
        var vec = player.getEyeLocation.getDirection
        vec.setY(0.5)
        ent.setVelocity(vec.multiply(multi*2)) 
        }
    }
  }

  
  object first_skill extends Skill("Spin Attack",4,4,4){
    def runSkill(level : Int , player : Player){
      DamageManager.damageNearbyWithout(player.getLocation, 3, 3, 3, DamageManager.getDamageFor(player)*1.5, player)
        player.getWorld.playEffect(player.getLocation.add(1.5,1,0), Effect.CRIT , 0)
        player.getWorld.playEffect(player.getLocation.add(-1.5,1,0), Effect.CRIT , 0)
        player.getWorld.playEffect(player.getLocation.add(0.9,1,0.9), Effect.CRIT , 0)
        player.getWorld.playEffect(player.getLocation.add(-0.9,1,0.9), Effect.CRIT , 0)
        player.getWorld.playEffect(player.getLocation.add(0.9,1,-0.9), Effect.CRIT , 0)
        player.getWorld.playEffect(player.getLocation.add(-0.9,1,-0.9), Effect.CRIT , 0)
        player.getWorld.playEffect(player.getLocation.add(0,1,1.5), Effect.CRIT , 0)
        player.getWorld.playEffect(player.getLocation.add(0,1,-1.5), Effect.CRIT , 0)
    }
  }
   
  object third_skill extends Skill("Multi hit",7,7,7){
    def runSkill(level : Int , player : Player){
      new MultiHitter(player,level)
    }
  }
  
  object fourth_skill extends Skill("Smoke Bomb",10,10,10){
     def runSkill(level : Int , player : Player){
       player
     }
  }
  

  
}

class SmokeBombDamage(){
  
}

class MobKiller(delay : Int , entity : Entity) extends BukkitRunnable{
  this.runTaskLater(MainClass.instance, delay)
  override def run(){  
    entity.remove()
  }
}