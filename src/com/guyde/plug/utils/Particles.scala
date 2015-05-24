package com.guyde.plug.utils

import org.bukkit.Location
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.util.Vector
import org.bukkit.scheduler.BukkitRunnable
import com.guyde.plug.main.MainClass
import java.util.Random

abstract class AbstractParticle(var loc : Location){
  final def world = loc.getWorld
  final def pos = loc.toVector
  def spawn() : Unit
}

class DelayedParticle(particle : AbstractParticle , delay : Int) extends BukkitRunnable(){
  this.runTaskLater(MainClass.instance,delay)
  final def run(){
    particle.spawn()
  }
}

class BasicParticle(location : Location, val particle : Effect, val data : Int) extends AbstractParticle(location){
  def spawn(){
    world.playEffect(loc, particle, data)
  }
}

class BlockParticle(location : Location, val block : Material) extends BasicParticle(location,Effect.TILE_BREAK,block.getId)

class RandomParticle(location : Location, particle : Effect, data : Int, radius : Double) extends BasicParticle(location,particle,data){
 
  override def spawn(){
    val x = new Random().nextInt((radius*2*100).toInt)-100*radius
    val y = new Random().nextInt((radius*2*100).toInt)-100*radius
    val z = new Random().nextInt((radius*2*100).toInt)-100*radius
    world.playEffect(loc.clone.add(x/100d,y/100d,z/100d) , particle, data)
    //world.playEffect(loc , particle, data)
  }
  
}



/**
 * @param particle the spawned particle
 * @param radius the radius of the circle
 * @param data the data value of the particle
 */
class CircleParticleY(val particle : Effect, location : Location ,val radius : Double, val data : Int) extends AbstractParticle(location){
  
  private val length : Int = (radius * 16).toInt
  
  def spawn(){
    for (i <- 1 to length){
      val x = Math.cos(Math.toRadians((i.toDouble/length)*360d)) * radius
      val z = Math.sin(Math.toRadians((i.toDouble/length)*360d)) * radius
      world.playEffect(loc.clone.add(x, 0, z), particle, data)
    }
  }
  
}


/**
 * @param particle the spawned particle
 * @param radius the radius of the circle
 * @param data the data value of the particle
 */
class CircleParticleX(val particle : Effect, location : Location ,val radius : Double, val data : Int) extends AbstractParticle(location){
  
  private val length : Int = (radius * 16).toInt
  
  def spawn(){
    for (i <- 1 to length){
      val y = Math.cos(Math.toRadians((i.toDouble/length)*360d)) * radius
      val z = Math.sin(Math.toRadians((i.toDouble/length)*360d)) * radius
      world.playEffect(loc.clone.add(0, y, z), particle, data)
    }
  }
  
}


/**
 * @param particle the spawned particle
 * @param radius the radius of the circle
 * @param data the data value of the particle
 */
class CircleParticleZ(val particle : Effect, location : Location ,val radius : Double, val data : Int) extends AbstractParticle(location){
  
  private val length : Int = (radius * 16).toInt
  
  def spawn(){
    for (i <- 1 to length){
      val x = Math.cos(Math.toRadians((i.toDouble/length)*360d)) * radius
      val y = Math.sin(Math.toRadians((i.toDouble/length)*360d)) * radius
      world.playEffect(loc.clone.add(x, y, 0), particle, data)
    }
  }
  
}


class CircleParticleYDelayed(val particle : BasicParticle, val radius : Double, val delay : Int, val ang : Int) extends AbstractParticle(particle.loc){
  
  private val length : Int = (radius * 12).toInt
  
  def spawn(){
    for (i <- 1 to length){
      val x = Math.cos(Math.toRadians((i.toDouble/length)*360d+ang)) * radius
      val z = Math.sin(Math.toRadians((i.toDouble/length)*360d+ang)) * radius
      new DelayedParticle(new BasicParticle(loc.clone.add(x,0,z),particle.particle,particle.data),i*delay)
    }
  }
  
}

class CircleParticleXDelayed(val particle : BasicParticle, val radius : Double, val delay : Int, val ang : Int) extends AbstractParticle(particle.loc){
  
  private val length : Int = (radius * 12).toInt
  
  def spawn(){
    for (i <- 1 to length){
      val y = Math.cos(Math.toRadians((i.toDouble/length)*360d+ang)) * radius
      val z = Math.sin(Math.toRadians((i.toDouble/length)*360d+ang)) * radius
      new DelayedParticle(new BasicParticle(loc.clone.add(0,y,z),particle.particle,particle.data),i*delay)
    }
  }
  
}

class CircleParticleZDelayed(val particle : BasicParticle, val radius : Double, val delay : Int, val ang : Int) extends AbstractParticle(particle.loc){
  
  private val length : Int = (radius * 12).toInt
  
  def spawn(){
    for (i <- 1 to length){
      val x = Math.cos(Math.toRadians((i.toDouble/length)*360d+ang)) * radius
      val y = Math.sin(Math.toRadians((i.toDouble/length)*360d+ang)) * radius
      new DelayedParticle(new BasicParticle(loc.clone.add(x,y,0),particle.particle,particle.data),i*delay)
    }
  }
  
}