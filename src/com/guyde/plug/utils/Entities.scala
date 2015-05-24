package com.guyde.plug.utils

import scala.reflect.ClassTag
import scala.reflect.classTag
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.scheduler.BukkitRunnable
import com.guyde.plug.main.MainClass
import java.util.UUID
import org.bukkit.entity.EntityType

abstract class ListenedProjectile(val owner : LivingEntity, yaw_offset : Float, force : Double,e_type : EntityType ){
  var proj : Projectile = null
  var uuid : UUID = null
  val world : World = owner.getWorld
  final def spawn(){
    var loc = owner.getEyeLocation.clone
    loc.setYaw(loc.getYaw + yaw_offset)
    val dir = loc.getDirection
    proj = world.spawn(owner.getEyeLocation.clone.add(dir),  e_type.getEntityClass).asInstanceOf[Projectile]
    proj.setShooter(owner.asInstanceOf[ProjectileSource])
    proj.setMetadata("listened", new FixedMetadataValue(MainClass.instance,this))
    proj.setVelocity(dir.multiply(force))
    uuid = proj.getUniqueId
    new Updator()
  }
  
  private var hitted : Boolean = false
  
  val updated : Boolean 
  
  private class Updator() extends BukkitRunnable(){
    if (updated && !hitted){
      this.runTaskLater(MainClass.instance, 1)
    }
    final def run(){
      onUpdate()
      new Updator()
    }
    
  }
  def onUpdate() : Unit
  
  def onHit(){
    hitted = true
    onHitting()
  }
  
  def onHitting() : Unit
}

class DemaEntity(val isUpdated : Boolean , private var pos : Location){ 
  
  private var alive = 0
  def timeAlive = alive
  private var update = isUpdated
  def posX : Double = pos.getX
  def posY : Double = pos.getY
  def posZ : Double = pos.getZ
  def world : World = pos.getWorld
  
  
  def setPosition(position : Location){
    pos = position
  }
  
  def getLocation = pos
  
  private var function : (DemaEntity) => Unit = null
  
  def onUpdate(func : (DemaEntity) => Unit){
    function = func
  }
  
  
  
  private class Killer(ticks : Int) extends BukkitRunnable(){
    this.runTaskLater(MainClass.instance, ticks)
    final def run(){
      kill()
    }
  }
  
  class EntityUpdator() extends BukkitRunnable(){
    private var ent = DemaEntity.this
    this.runTaskLater(MainClass.instance, 1)
    final def run(){
      if (!update) return
      if (function!=null){
        alive=alive+1
        function.apply(ent)
      }
      new EntityUpdator()
    }
  }
  

  
  new EntityUpdator()
  
  def spawnEffect(effect : Effect , data : Int){
    world.playEffect(pos, effect, data)
  }
  
  def killIn(ticks : Int){
    new Killer(ticks)
  }
  
  def kill(){
    update = false
  }
}