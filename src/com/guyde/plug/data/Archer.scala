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
import org.bukkit.entity.LivingEntity
class Archer(uuid : UUID) extends GameClass(uuid,Clicks.Left,Material.BOW){
  var name = "Archer"
  var chatname = "Ar"
  var level_requirement = 0
  object second_skill extends Skill("Escape",8,7,6){
    def runSkill(level : Int , player : Player){
      if (player.isOnGround){
        var lookVector = player.getEyeLocation.getDirection
        player.setVelocity(lookVector.multiply(-(1.5f+level/2f)).multiply(new org.bukkit.util.Vector(1.6f,1f,1.6f)))
      }
    }
  } 
  
  class ArrowSummoner(vec : Vector , player : Player) extends BukkitRunnable(){
    override def run(){
      var arrow = player.getWorld.spawnArrow(player.getEyeLocation.subtract(0,0.25,0).add(vec), new Vector(0,0,0), 0.6f, 12)
      arrow.setVelocity(vec)
      arrow.setShooter(player.asInstanceOf[ProjectileSource])
      arrow.setMetadata("dmg_multi", new FixedMetadataValue(MainClass.instance,java.lang.Float.valueOf(0.4f)))
    }
  }
  
  class BombArrowSummoner(vec : Vector , player : Player , bool : Boolean) extends BukkitRunnable(){
    override def run(){
      var arrow = player.getWorld.spawnArrow(player.getEyeLocation.subtract(0,0.5,0).add(vec),new Vector(0,0,0), 0.6f, 12)
      arrow.setVelocity(vec)
      arrow.setShooter(player.asInstanceOf[ProjectileSource])
      arrow.setMetadata("dmg_multi", new FixedMetadataValue(MainClass.instance,java.lang.Float.valueOf(2.5f)))
      new BombArrowTracker(arrow,new Random().nextInt(4)+1,0).runTaskLater(MainClass.instance, 1)
      
    }
    class BombArrowTracker(arrow : Arrow,max : Int , cur : Int) extends BukkitRunnable(){
      override def run(){
        var loc = arrow.getLocation.clone
        arrow.getWorld.playEffect(loc, Effect.HAPPY_VILLAGER, 5) 
        if (!arrow.isOnGround()){
          new BombArrowTracker(arrow,max,cur).runTaskLater(MainClass.instance,1)
        } else {
          val loc1 = new org.bukkit.util.Vector(8,10,6).toLocation(arrow.getWorld())
        //  loc1.setPitch(new Random().nextInt(360))
         loc1.setYaw(new Random().nextInt(360))
         DamageManager.damageNearbyWithout(arrow.getLocation, 5,5,5, DamageManager.getDamageFor(JavaHelper.getShooter(arrow).asInstanceOf[Player]),JavaHelper.getShooter(arrow).asInstanceOf[Player])
          val nArrow =arrow.getWorld.spawnArrow(loc.add(0, 0.1, 0),loc1.toVector, 0.6f, 12)
   //       nArrow.setVelocity(loc1.toVector)
          nArrow.getWorld.playEffect(arrow.getLocation, Effect.EXPLOSION_HUGE, 0)
          nArrow.setMetadata("dmg_multi", new FixedMetadataValue(MainClass.instance,2.5f))
                nArrow.setShooter(player.asInstanceOf[ProjectileSource])
                arrow.remove()
                 if (cur+1!=max){           new BombArrowTracker(nArrow,max,cur+1).runTaskLater(MainClass.instance,1)
                    arrow.remove(); return}
          nArrow.remove

        }
        
        
      }
    }
  }
  
  object first_skill extends Skill("Arrow Storm",6,5,4){
    def runSkill(level : Int , player : Player){
     
      for (i <-1 to 20){

        var copy = player.getEyeLocation.clone()
        copy.setYaw(copy.getYaw+2-new Random().nextInt(5))
        copy.setPitch(copy.getPitch+2-new Random().nextInt(5))
        var copy1 = player.getEyeLocation.clone()
        copy1.setYaw(copy1.getYaw+25+new Random().nextInt(5))
        copy1.setPitch(copy1.getPitch+2-new Random().nextInt(5))
        var copy2 = player.getEyeLocation.clone()
        copy2.setYaw(copy2.getYaw-25-new Random().nextInt(5))
        copy2.setPitch(copy2.getPitch+2-new Random().nextInt(5))
        new ArrowSummoner(copy.getDirection,player).runTaskLater(MainClass.instance, i+3)
        new ArrowSummoner(copy1.getDirection,player).runTaskLater(MainClass.instance, i+3)
        new ArrowSummoner(copy2.getDirection,player).runTaskLater(MainClass.instance, i+3)
      }
      
    }
  }
   
  object third_skill extends Skill("Bomb Arrow",7,6,5){
    def runSkill(level : Int , player : Player){
      var copy = player.getEyeLocation.clone()
      copy.setYaw(copy.getYaw+2-new Random().nextInt(5))
      if (level==3){
        new BombArrowSummoner(copy.getDirection,player,true).runTaskLater(MainClass.instance, 3)
        return
      }
      new BombArrowSummoner(copy.getDirection,player,false).runTaskLater(MainClass.instance, 3)
    }
  }
  
  object fourth_skill extends Skill("Arrow Shield",10,10,10){
     def runSkill(level : Int , player : Player){
       var arrow1 = player.getWorld.spawnArrow(player.getEyeLocation.add(0,0,1), new Vector(0,-1,0), 0.6f, 12)
       arrow1.setShooter(player)
       new ArrowShieldTracker(arrow1,player,new Vector(0,0,1)).runTaskLater(MainClass.instance, 1)
       var arrow2 = player.getWorld.spawnArrow(player.getEyeLocation.add(0,0,-1), new Vector(0,-1,0), 0.6f, 12)
       arrow2.setShooter(player)
       new ArrowShieldTracker(arrow2,player,new Vector(0,0,-1)).runTaskLater(MainClass.instance, 1)
       var arrow3 = player.getWorld.spawnArrow(player.getEyeLocation.add(0.7,0,0.7), new Vector(0,-1,0), 0.6f, 12)
       arrow3.setShooter(player)
       new ArrowShieldTracker(arrow3,player,new Vector(0.7,0,0.7)).runTaskLater(MainClass.instance, 1)
       var arrow4 = player.getWorld.spawnArrow(player.getEyeLocation.add(0.7,0,-0.7), new Vector(0,-1,0), 0.6f, 12)
       arrow4.setShooter(player)
       new ArrowShieldTracker(arrow4,player,new Vector(0.7,0,-0.7)).runTaskLater(MainClass.instance, 1)
       var arrow5 = player.getWorld.spawnArrow(player.getEyeLocation.add(-0.7,0,0.7), new Vector(0,-1,0), 0.6f, 12)
       arrow5.setShooter(player)
       new ArrowShieldTracker(arrow5,player,new Vector(-0.7,0,0.7)).runTaskLater(MainClass.instance, 1)
       var arrow6 = player.getWorld.spawnArrow(player.getEyeLocation.add(-0.7,0,-0.7), new Vector(0,-1,0), 0.6f, 12)
       arrow6.setShooter(player)
       new ArrowShieldTracker(arrow6,player,new Vector(-0.7,0,-0.7)).runTaskLater(MainClass.instance, 1)
       var arrow7 = player.getWorld.spawnArrow(player.getEyeLocation.add(1,0,0), new Vector(0,-1,0), 0.6f, 12)
       arrow7.setShooter(player)
       new ArrowShieldTracker(arrow7,player,new Vector(1,0,0)).runTaskLater(MainClass.instance, 1)
       var arrow8 = player.getWorld.spawnArrow(player.getEyeLocation.add(-1,0,0), new Vector(0,-1,0), 0.6f, 12)
       arrow8.setShooter(player)
       new ArrowShieldTracker(arrow8,player,new Vector(-1,0,0)).runTaskLater(MainClass.instance, 1)
     }
  }
  
  class ArrowShieldTracker(arrow : Arrow , player : Player , offset : Vector) extends BukkitRunnable{
    override def run(){
        var loc = arrow.getLocation.clone
        arrow.setVelocity(new Vector(0,-0.1,0))
     //  arrow.getWorld.playEffect(loc, Effect.FIREWORKS_SPARK, 5)
            arrow.getWorld.playEffect(loc, Effect.FIREWORKS_SPARK, 0)    
        if (arrow.isOnGround()){
          var nArrow = arrow.getWorld.spawnArrow(player.getEyeLocation.add(offset), new Vector(0,-1,0), 0.6f, 12)
          nArrow.setShooter(player)
          new ArrowShieldTracker(nArrow,player,offset).runTaskLater(MainClass.instance, 25)
          arrow.remove()
          
        } else {
            new ArrowShieldTracker(arrow,player,offset).runTaskLater(MainClass.instance, 1)
        }
    }
  }
}
