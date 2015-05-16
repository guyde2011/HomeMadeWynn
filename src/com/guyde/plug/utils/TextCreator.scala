package com.guyde.plug.utils

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.EntityType
import org.bukkit.entity.ArmorStand
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.entity.Entity
import org.bukkit.scheduler.BukkitRunnable
import com.guyde.plug.main.MainClass
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftArmorStand
import net.minecraft.server.v1_8_R2.NBTTagCompound

class CustomNameTracker(val entity : Entity , val offset : org.bukkit.util.Vector , val name : ArmorStand){
  var on : Boolean = false; 
 
  def start(){
    new Tracker().runTaskLater(MainClass.instance, 1)
  }
    
  def updateName(str : String){
    name.setCustomName(str)
  }
    
  private class Tracker() extends BukkitRunnable{
    override def run(){
      if (name.isDead()){return}
      if (entity.isDead){
        name.remove()
      } else {
        name.teleport(entity.getLocation.add(offset))
        new Tracker().runTaskLater(MainClass.instance, 1)
      }
    }
  }
}

object TextCreator{
  final def createTrackedText(text : String , entity : Entity , offset : org.bukkit.util.Vector) : CustomNameTracker = {
    var copy = entity.getLocation.clone
    var armor_stand = entity.getWorld.spawnEntity(copy.add(offset), EntityType.ARMOR_STAND).asInstanceOf[ArmorStand]
    var nms = armor_stand.asInstanceOf[CraftArmorStand].getHandle
    var comp = nms.getNBTTag
    if (comp==null){
      comp = new NBTTagCompound();
    }
    nms.c(comp)
    comp.setInt("NoAI", 1)
    nms.f(comp)
    armor_stand.setCustomName(text)
    armor_stand.setGravity(false)
    armor_stand.setCustomNameVisible(true)
    armor_stand.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,1000000,1), true)
    armor_stand.setVisible(false)
    var track = new CustomNameTracker(entity,offset,armor_stand)
    armor_stand.setMetadata("tracking", new FixedMetadataValue(MainClass.instance,entity))
    track.start
    return track;
  }
  
  
  
  final def createTextAt(text : String , loc : Location , delay : Int){
    var copy = loc.clone()
    var armor_stand = loc.getWorld.spawnEntity(copy, EntityType.ARMOR_STAND).asInstanceOf[ArmorStand]
    var nms = armor_stand.asInstanceOf[CraftArmorStand].getHandle
    var comp = nms.getNBTTag
    if (comp==null){
      comp = new NBTTagCompound();
    }
    nms.c(comp)
    comp.setInt("NoAI", 1)
    nms.f(comp)
    armor_stand.setCustomName(text)
    armor_stand.setGravity(false)
    armor_stand.setCustomNameVisible(true)
    armor_stand.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,1000000,1), true)
    armor_stand.setVisible(false)
    new CustomNameKiller(armor_stand , delay)
  }
  
  private class CustomNameKiller(name : ArmorStand , delay : Int) extends BukkitRunnable{
    this.runTaskLater(MainClass.instance, delay)
    override def run(){
      name.remove
    }
  }
}