package com.guyde.plug.utils

import scala.reflect._
import scala.reflect.ClassTag

import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.projectiles.ProjectileSource
import org.bukkit.scheduler.BukkitRunnable

import com.guyde.plug.main.MainClass

abstract class ListenedProjectile[T >: Projectile](implicit val tag : ClassTag[T],val owner : LivingEntity , pitch_offset : Float , yaw_offset : Float , force : Double){
  final def spawn(){
    var loc = owner.getEyeLocation.clone
    loc.setYaw(loc.getYaw + yaw_offset)
    loc.setPitch(loc.getPitch + pitch_offset)
    val dir = loc.getDirection
    val proj = owner.getWorld.spawn(owner.getEyeLocation.clone.add(owner.getEyeLocation.getDirection),  classTag[T].runtimeClass.asSubclass(classOf[Projectile]))
    proj.setShooter(owner.asInstanceOf[ProjectileSource])
    proj.setMetadata("listened", new FixedMetadataValue(MainClass.instance,this))
    proj.setVelocity(owner.getEyeLocation.getDirection.multiply(force))
  }
  
  val updated : Boolean 
  
  def onUpdate() : Unit
 
  def onHit(proj : T , loc : Location) : Boolean
}

class DemaEntity(val isUpdated : Boolean , private var pos : Location){ 
  
  def setPosition(position : Location){
    pos = position
  }
  
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
        function.apply(ent)
      }
      new EntityUpdator()
    }
  }
  
  private var update = isUpdated
  def posX : Double = pos.getX
  def posY : Double = pos.getY
  def posZ : Double = pos.getZ
  def world : World = pos.getWorld
  
  
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