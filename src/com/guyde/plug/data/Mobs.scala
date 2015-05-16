package com.guyde.plug.data

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import com.guyde.plug.main.MainClass
import net.minecraft.server.v1_8_R2.NBTTagCompound
import org.bukkit.entity.Ageable
import org.bukkit.entity.Zombie
import com.guyde.plug.utils.TextCreator
import org.bukkit.util.Vector



class MetaHolder{
  private var meta_map = Map[String,Object]()
  
  def put(str : String , value : Object){
    meta_map = meta_map + (str -> value)
  }
  
  def putInt(str : String , value : Int){
    meta_map = meta_map + (str -> Integer.valueOf(value))
  }
  
  def putFloat(str : String , value : Float){
    meta_map = meta_map + (str -> java.lang.Float.valueOf(value))
  }
  
  def putDouble(str : String , value : Double){
    meta_map = meta_map + (str -> java.lang.Double.valueOf(value))
  }
  
  def putBoolean(str : String , value : Boolean){
    meta_map = meta_map + (str -> java.lang.Boolean.valueOf(value))
  }
  
  def attachTo(ent : Entity){
    meta_map.foreach(e => ent.setMetadata(e._1, new FixedMetadataValue(MainClass.instance,e._2)))
  }
  
  def get[T](str : String , cls : Class[T]) : T = {
    return meta_map(str).asInstanceOf[T]
  }
  
  def get(str : String) : Object = {
    return meta_map(str)
  }
  
  def merge(holder : MetaHolder) : MetaHolder = {
    holder.meta_map.foreach(e => put(e._1 , e._2))
    return this
  }
  
   def hasTag(str : String) : Boolean = {
    return meta_map.contains(str)
  }
}

class MobEquipment(){
  private var equipment = Array[Equipment]()
  private var held : Equipment = null
  
  implicit def +(stack : ItemStack) : MobEquipment = {
    if (stack!=null) {
      equipment = equipment :+ new Equipment(stack) 
    } else {
      equipment = equipment :+ new Equipment(new ItemStack(Material.AIR)) 
    }
    return this
  }
  
  implicit def +(stack : LuaValue) : MobEquipment = {
    if (stack!=null) {
      equipment = equipment :+ new Equipment(stack.asInstanceOf[LuaTable]) 
    } else {
      equipment = equipment :+ new Equipment(new ItemStack(Material.AIR)) 
    }
    return this
  }
  
  implicit def +(stack : net.minecraft.server.v1_8_R2.ItemStack) : MobEquipment = {
    return this + (CraftItemStack.asBukkitCopy(stack)) 
  }
  
  implicit def ++ (stack : net.minecraft.server.v1_8_R2.ItemStack) : MobEquipment = {
    return this ++ (CraftItemStack.asBukkitCopy(stack)) 
  }
  
  implicit def ++(stack : ItemStack) : MobEquipment = {
    if (stack!=null) {
      held = new Equipment(stack) 
    } else {
      held = new Equipment(new ItemStack(Material.AIR)) 
    }
     return this
  }
  
  implicit def ++(stack : LuaValue) : MobEquipment = {
    if (stack!=null) {
      held = new Equipment(stack.asInstanceOf[LuaTable]) 
    } else {
      held = new Equipment(new ItemStack(Material.AIR)) 
    }
     return this
  }
  
  def getHelmet() : ItemStack = {
    return equipment(0).stack
  }
  
  def getChestplate() : ItemStack = {
    return equipment(1).stack
  }
  
  def getLeggings() : ItemStack = {
    return equipment(2).stack
  }
  
  def getBoots() : ItemStack = {
    return equipment(3).stack
  }
  
  def setHelmet(equip : Equipment) = {
    equipment(0) = equip
  }
  
  def setChestplate(equip : Equipment) = {
    equipment(1) = equip
  }
  
  def setLeggings(equip : Equipment) = {
    equipment(2) = equip
  }
  
  def setBoots(equip : Equipment) = {
    equipment(3) = equip
  }
  
  def setHeldItem(equip : Equipment) = {
    held = equip
  }
  
  def getHeldItem() : ItemStack = {
    return held.stack
  }
  

  
  
  def gear(ent : LivingEntity){
    ent.getEquipment.setBoots(getBoots)
    ent.getEquipment.setLeggings(getLeggings)
    ent.getEquipment.setChestplate(getChestplate)
    ent.getEquipment.setHelmet(getHelmet)
    ent.getEquipment.setItemInHand(getHeldItem)
    ent.getEquipment.setBootsDropChance(0)
    ent.getEquipment.setHelmetDropChance(0)
    ent.getEquipment.setLeggingsDropChance(0)
    ent.getEquipment.setChestplateDropChance(0)
    ent.getEquipment.setItemInHandDropChance(0)
  }
  
  class Equipment(){
    private var stack_ : ItemStack = null;
    def stack : ItemStack = stack_
    
    def this(Stack : ItemStack){
      this()
      stack_ =Stack
    }
    
    def this(value : LuaTable){
      this()
      var material = value.get("item").tojstring()
      var color : Int = 0
      if (value.get("color")!=null && !value.get("color").equals(LuaValue.NIL)){
        color = value.get("color").toint()
      }
      var meta = value.get("meta").toshort()
      var _stack_ = new ItemStack(Material.getMaterial(material),1,meta)
      var nstack = CraftItemStack.asNMSCopy(_stack_)
      var tag = nstack.getTag
      if (tag==null){
        tag = new NBTTagCompound()
      }
      
      var display = new NBTTagCompound()
 
      display.setInt("color",color)
      tag.set("display",display)
      nstack.setTag(tag)
      stack_ = CraftItemStack.asBukkitCopy(nstack)
    }
  }

}

class WynnMob(holder : MetaHolder , ent : EntityType ){
  
  def createMob(loc : Location){
    var spawned = loc.getWorld.spawnEntity(loc, ent)
    holder.attachTo(spawned)
    var name = holder.get("name").asInstanceOf[String]
    TextCreator.createTrackedText(name, spawned, new Vector(0,0.125,0))
    if (spawned.isInstanceOf[Ageable]){
      spawned.asInstanceOf[Ageable].setAdult()
    }
    if (spawned.isInstanceOf[Zombie]){
      spawned.asInstanceOf[Zombie].setVillager(false)
      spawned.asInstanceOf[Zombie].setBaby(false)
    }
    if (spawned.isInstanceOf[LivingEntity]){
      spawned.asInstanceOf[LivingEntity].setMaxHealth(holder.get("max_hp").asInstanceOf[Integer].toDouble)
      spawned.asInstanceOf[LivingEntity].setHealth(JavaHelper.getMaxHealth(spawned.asInstanceOf[LivingEntity]))
    }
    if (spawned.isInstanceOf[LivingEntity] && holder.hasTag("equipment")){
      var equipment = holder.get("equipment", classOf[MobEquipment])
      equipment.gear(spawned.asInstanceOf[LivingEntity])
     
    }
  }
  
}

object WynnMobs{
  private var hostiles = Map[String,WynnAggressive]()
  
  
  final def getHostile(str : String) : WynnAggressive = {
    return hostiles(str)
  }
  
  final def registerHostile(mob : WynnAggressive){
    hostiles = hostiles + (mob.unlocal -> mob)
  }
  
  final def formatName(level : Int , name : String , canAttack : Boolean) : String = {
    var color = ChatColor.RED
    if (!canAttack) color = ChatColor.GREEN
    return color + name + ChatColor.GOLD + " [Lv. " + level + "]" 
  }
  
  final def formatAgro(hp : Int , min_dmg : Double , max_dmg : Double , min_exp : Int , max_exp : Int , level : Int , name : String , unlocal : String , canAttack : Boolean , extraData : MetaHolder) : MetaHolder = {
    var holder = new MetaHolder
    holder.putDouble("min_dmg", min_dmg)
    holder.putDouble("max_dmg", max_dmg)
    holder.putDouble("min_exp", min_exp)
    holder.putDouble("max_exp", max_exp)
    holder.putInt("mob_level", level)
    holder.putInt("max_hp", hp)
    holder.put("WynnId" , unlocal)
    holder.putBoolean("canAttack",canAttack)
    holder.put("name",formatName(level,name,canAttack))
    return holder.merge(extraData)
  }
  
}

class WynnAggressive(hp : Int , min_dmg : Double , max_dmg : Double , min_exp : Int , max_exp : Int , level : Int , name : String , val unlocal : String , canAttack : Boolean , extra : MetaHolder , ent : EntityType) extends WynnMob(WynnMobs.formatAgro(hp,min_dmg, max_dmg, min_exp, max_exp, level, name, unlocal, canAttack, extra),ent){
  
  def register = WynnMobs.registerHostile(this)
}