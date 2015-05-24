package com.guyde.plug.data

import java.util.UUID
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.entity.Egg
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import com.guyde.plug.main.MainClass
import com.guyde.plug.utils.BasicParticle
import com.guyde.plug.utils.CircleParticleYDelayed
import com.guyde.plug.utils.ListenedProjectile
import com.guyde.plug.utils.DemaEntity
import com.guyde.plug.utils.RandomParticle
import org.bukkit.entity.EntityType

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
      val basic = new BasicParticle(player.getEyeLocation.clone,Effect.CRIT,0)
      val basic1 = new BasicParticle(player.getEyeLocation.clone.add(0,0.25,0),Effect.CRIT,0)
      val basic2 = new BasicParticle(player.getEyeLocation.clone.add(0,-0.25,0),Effect.CRIT,0)
      new CircleParticleYDelayed(basic,2.5d,1,10).spawn()
      new CircleParticleYDelayed(basic,2.5d,1,0).spawn()
      new CircleParticleYDelayed(basic,2.5d,1,5).spawn()
      new CircleParticleYDelayed(basic1,2.5d,1,10).spawn()
      new CircleParticleYDelayed(basic1,2.5d,1,0).spawn()
      new CircleParticleYDelayed(basic1,2.5d,1,5).spawn()
      new CircleParticleYDelayed(basic2,2.5d,1,10).spawn()
      new CircleParticleYDelayed(basic2,2.5d,1,0).spawn()
      new CircleParticleYDelayed(basic2,2.5d,1,5).spawn()
    }
  }
   
  object third_skill extends Skill("Multi hit",7,7,7){
    def runSkill(level : Int , player : Player){
      new MultiHitter(player,level)
    }
  }
  
  object fourth_skill extends Skill("Smoke Bomb",7,7,7){
    def runSkill(level : Int , player : Player){
      level match {
        case 1 => new SmokeBombEgg(player,0,false).spawn
        case 2 => new SmokeBombEgg(player,0,true).spawn
        case 3 => {new SmokeBombEgg(player,0,true).spawn;new SmokeBombEgg(player,25,true).spawn;new SmokeBombEgg(player,-25,true).spawn}
      }
    }
  }
  

  
}

class SmokeBombEgg(player : Player, offset : Float, slow : Boolean) extends ListenedProjectile(player,offset,1,EntityType.EGG){
  val updated = true
  def onUpdate(){
    
  }
  
  def onHitting(){
    val ent = new DemaEntity(true,proj.getLocation)
    ent.onUpdate { 
      a => 
      if (a.timeAlive>120){
        a.kill
      } else {
        new RandomParticle(ent.getLocation,Effect.SMOKE,0,1.5d).spawn
        if (slow) DamageManager.getNearbyWithout(ent.getLocation, 1.5d, 1.5d, 1.5d, player).forall { x => x.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,1,1)) }
          
        if (a.timeAlive%10==0){
          DamageManager.damageNearbyWithout(ent.getLocation, 1.5d, 1.5d, 1.5D, DamageManager.getDamageFor(player)*0.6, player)
        }
      }
    }
  }
  
}

class MobKiller(delay : Int , entity : Entity) extends BukkitRunnable{
  this.runTaskLater(MainClass.instance, delay)
  override def run(){  
    entity.remove()
  }
}