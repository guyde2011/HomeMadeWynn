package com.guyde.plug.data

import java.util.UUID
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.bukkit.Effect
import com.guyde.plug.main.MainClass
class Warrior(uuid : UUID) extends GameClass(uuid,Clicks.Right,Material.IRON_SPADE){
  var name = "Warrior"
  var chatname = "Wa"
  var level_requirement = 0
  object second_skill extends Skill("Charge",4,4,4){
    def runSkill(level : Int , player : Player){
      player.setVelocity(player.getLocation.getDirection.multiply(new Vector((level+11d)/3d,1.2d,(level+11d)/3d)))
    }
  } 
  
  class BashExplosion(player : Player , radius : Double) extends BukkitRunnable(){
    final def run(){
      DamageManager.damageNearbyWithout(player.getLocation, radius,radius,radius, DamageManager.getDamageFor(player)*1.3, player)
      player.getWorld.playEffect(player.getLocation, Effect.EXPLOSION_LARGE, 0)
    }    
  }
  
  object first_skill extends Skill("Bash",5,5,5){
    def runSkill(level : Int , player : Player){
      new BashExplosion(player,5).runTaskLater(MainClass.instance, 10)
      if (level > 1){
       new BashExplosion(player,6).runTaskLater(MainClass.instance, 30)
      }
    }
  }
   
  object third_skill extends Skill("Uppercut",5,5,5){
    def runSkill(level : Int , player : Player){

    }
  }
  
  object fourth_skill extends Skill("War Scream",7,6,5){
     def runSkill(level : Int , player : Player){
    
     }
  }
  

  
}


