package com.guyde.plug.data;

import java.util.Random
import scala.collection.JavaConversions._
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import net.minecraft.server.v1_8_R2.NBTTagCompound
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector
import org.bukkit.World
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Bukkit
import java.util.Collection
import com.guyde.plug.main.MainClass


class WynnItem(name : String , lore : Array[String] , item : Material , meta : Short , compound : NBTTagCompound){
  def this(name : String , lore : Array[String] , item : Material , meta : Short) = { 
    this(name,lore,item,meta,new NBTTagCompound())
  }
  final def CreateItemStack() : ItemStack = {
    var bukkit_stack = new ItemStack(item,1,meta)
    var NMS_stack = CraftItemStack.asNMSCopy(bukkit_stack)
    NMS_stack.setTag(compound)
    bukkit_stack = CraftItemStack.asBukkitCopy(NMS_stack)
    var item_meta = bukkit_stack.getItemMeta
    item_meta.setDisplayName(name)
    item_meta.setLore(lore.toList)
    bukkit_stack.setItemMeta(item_meta)
    return bukkit_stack
  }
}

abstract class Identify(name : String , unlocal_name : String){
  final def getName = name
  final def getId = unlocal_name
  def format(lvl : Int) : String
  
}

object WynnIds{
  
  object LootBonus extends Identify("Loot Bonus","loot"){
    def format(lvl : Int) : String = {
      return "+" + lvl + ".0"
    }
  }
  
  object SpellDmg extends Identify("Spell Damage","dmg"){
    def format(lvl : Int) : String = {
      return "+" + lvl + ".0%"
    }
  }
  
  object XpBonus extends Identify("XP Bonus","xp"){
    def format(lvl : Int) : String = {
      return "+" + lvl + ".0%"
    }
  }
  
  object ManaRegen extends Identify("Mana Regen","mreg"){
    def format(lvl : Int) : String = {
      return lvl + ""
    }
  }
  
  object HealthRegen extends Identify("Health Regen","hreg"){
    def format(lvl : Int) : String = {
      return lvl / 10d + ""
    }
  }
  
  object ManaSteal extends Identify("Mana Steal","mstl"){
    def format(lvl : Int) : String = {
      return lvl + ""
    }
  }
  
  object LifeSteal extends Identify("Life Steal","hstl"){
    def format(lvl : Int) : String = {
      return lvl / 10d + ""
    }
  }
  
  def getId(id : String) : Identify = {
    id match{
      case "hstl" => return LifeSteal
      case "mstl" => return ManaSteal
      case "hreg" => return HealthRegen
      case "mreg" => return ManaRegen
      case "xp" => return XpBonus
      case "dmg" => return SpellDmg
      case "loot" => return LootBonus
    }
    return null
  }
  
}

object WynnItems{
  
  val NORMAL = 0
  val UNIQUE = 1
  val RARE = 2
  val LEGENDARY = 3
  val SPECIAL = 4

  
  private var Weapons = Map[String,WynnWeapon]()
  private var Armors = Map[String,WynnArmor]()
  
  def registerWeapon(item : WynnWeapon){
    Weapons = Weapons + (item.getName -> item)
    DropRates.registerTier(item)
  }
  
  def registerArmor(item : WynnArmor){
    Armors = Armors + (item.getName -> item)
    DropRates.registerTier(item)
  }
  
  def formatName(name : String , tier : Int) : String = {
    tier match{
      case NORMAL => return ChatColor.RESET + name
      case UNIQUE => return ChatColor.YELLOW + name
      case RARE => return ChatColor.LIGHT_PURPLE + name
      case LEGENDARY => return ChatColor.AQUA + name
      case SPECIAL => return ChatColor.RESET + name
    }
    return name
  }
  
  def getWeapon(stack : ItemStack) : WynnWeapon = {
    var NMS_stack = CraftItemStack.asNMSCopy(stack)
    var tag = NMS_stack.getTag
    if (tag!=null && tag.hasKey("ItemId")){
      return Weapons(tag.getString("ItemId"))
    }
    return null
  }
  
  def getArmor(stack : ItemStack) : WynnArmor = {
    var NMS_stack = CraftItemStack.asNMSCopy(stack)
    var tag = NMS_stack.getTag
    if (tag!=null && tag.hasKey("ItemId")){
      return Armors(tag.getString("ItemId"))
    }
    return null
  }
  
  def getWeapon(name : String) : WynnWeapon = {
    return Weapons(name)
  }
  
  def getArmor(name : String) : WynnArmor = {
    return Armors(name)
  }
  
  def getComplex(stack : ItemStack) : TierableItem = {
    var NMS_stack = CraftItemStack.asNMSCopy(stack)
    var tag = NMS_stack.getTag
    if (tag!=null && tag.hasKey("ItemId")){
      if (Armors.containsKey(tag.getString("ItemId"))){
        return Armors(tag.getString("ItemId"))
      } else {
        return Weapons(tag.getString("ItemId"))
      }
    }
    return null
  }
  
  def IdentifyItem(stack : ItemStack) : ItemStack = {
    var manager = getComplex(stack)
    var NMS_stack = CraftItemStack.asNMSCopy(stack)
    var tag = NMS_stack.getTag
    if (manager!=null){
      new scala.util.Random().shuffle(manager.Ids.toSeq).foreach{ID =>
        if (tag.hasKey("Ids")){
          var ids = tag.getCompound("Ids")
          if (!ids.hasKey(ID._1)){
            var max : Int = (ID._2 * 1.3).toInt
            var min : Int = (ID._2 * 0.7).toInt
            var lvl = Math.max(1,new Random().nextInt(max-min)+min)
            IdCompound(WynnIds.getId(ID._1) , lvl , tag)
            NMS_stack.setTag(tag)
            var ret = CraftItemStack.asBukkitCopy(NMS_stack)
            var meta = ret.getItemMeta
            var lore = meta.getLore
            lore = lore ++ List[String](formatIdentify(WynnIds.getId(ID._1),lvl))
            meta.setLore(lore)
            ret.setItemMeta(meta)
            return ret
          }
        } else {
          var max : Int = (ID._2 * 1.3).toInt
          var min : Int = (ID._2 * 0.7).toInt
          var lvl = Math.max(1,new Random().nextInt(max-min)+min)
          IdCompound(WynnIds.getId(ID._1) , lvl , tag)
          NMS_stack.setTag(tag)
          var ret = CraftItemStack.asBukkitCopy(NMS_stack)
          var meta = ret.getItemMeta
          var lore = meta.getLore
          lore = lore ++ List[String](formatIdentify(WynnIds.getId(ID._1),lvl))
          meta.setLore(lore)
          ret.setItemMeta(meta)
          return ret
        }
      }
    }
    return stack
  }
 
  
  def IdCompound(id : Identify , level : Int , comp : NBTTagCompound){
    if (!comp.hasKey("Ids")){
      comp.set("Ids",new NBTTagCompound())
    }
    var ids = comp.getCompound("Ids")
    ids.setInt(id.getId,level)
    comp.set("Ids",ids)
  }
  
  def weaponCompound(min_dmg : Int , max_dmg : Int , level : Int , name : String) : NBTTagCompound = {
    val comp = new NBTTagCompound()
    comp.setInt("min_dmg", min_dmg)
    comp.setInt("max_dmg", max_dmg)
    comp.setInt("lvl", level)
    comp.setBoolean("Unbreakable", true)
    comp.setString("ItemId",name)
    return comp
  }
  
  def armorCompound(defense : Int , level : Int , name : String) : NBTTagCompound = {
    val comp = new NBTTagCompound()
    comp.setInt("def", defense)
    comp.setInt("lvl", level)
    comp.setBoolean("Unbreakable", true)
    comp.setString("ItemId",name)
    return comp
  }
  
  def formatWeapon(min_dmg : Int , max_dmg : Int , level : Int , tier : Int) : Array[String] = {
    var lore = Array[String]("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "Dam: " + min_dmg + "-" + max_dmg)
    lore = lore :+ formatLevel(level)
    lore = lore :+ formatTier(tier)
    return lore
  }
  
  def formatArmor(defense : Int , level : Int , tier : Int) : Array[String] = {
    var lore = Array[String]()
    lore = lore :+ ("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "Def: " + defense)
    lore = lore :+ formatLevel(level)
    lore = lore :+ formatTier(tier)
    return lore
  }
  
  private def formatLevel(level : Int) : String = {
    return ChatColor.GOLD + "Lv. min " + level
  }
  
  def formatIdentify(id : Identify , level : Int) : String = {
    return ChatColor.GRAY + id.getName + " " + id.format(level)
  }
  
  private def formatTier(tier : Int) : String = {
    tier match{
      case NORMAL => return ""
      case UNIQUE => return ChatColor.YELLOW + "Unique Item"
      case RARE => return ChatColor.LIGHT_PURPLE + "Rare Item"
      case LEGENDARY => return ChatColor.AQUA + "Legendary Item"
      case SPECIAL => return ChatColor.DARK_AQUA + "Special Item"
    }

  }
  
}

class TierableItem(name : String , item : Material , lore : Array[String] , compound : NBTTagCompound , val tier : Int ,val level : Int) extends WynnItem(WynnItems.formatName(name,tier),lore,item,0,compound){
  var Ids = Map[String,Int]()
  
  def addIdentify(id : Identify , level : Int) : TierableItem = {
    Ids = Ids + (id.getName -> level)
    return this
  }
  
}

class WynnWeapon(name : String , item : Material , min_dmg : Int , max_dmg : Int , level : Int , tier : Int) extends TierableItem(name , item , WynnItems.formatWeapon(min_dmg, max_dmg, level, tier) , WynnItems.weaponCompound(min_dmg, max_dmg, level , name) , tier , level){
  
  def getName = name
  override def addIdentify(id : Identify , level : Int) : WynnWeapon = {
    Ids = Ids + (id.getName -> level)
    return this
  }
  
  def register() : WynnWeapon = {
    WynnItems.registerWeapon(this)
    return this
  }
  
}


class WynnArmor(name : String , item : Material , defense : Int , level : Int , tier : Int) extends TierableItem(name , item , WynnItems.formatArmor(defense , level, tier) , WynnItems.armorCompound(defense, level , name) , tier , level){

  def getName = name
  
  def register() : WynnArmor = {
    WynnItems.registerArmor(this)
    return this
  }
  
  override def addIdentify(id : Identify , level : Int) : WynnArmor = {
    Ids = Ids + (id.getName -> level)
    return this
  }
  
}

class PlayerHealthRegen() extends BukkitRunnable(){
  final def run(){
    var online : Array[Player] = Bukkit.getOnlinePlayers()
    online.foreach { player => 
      var regen = IdentifiesHelper.getIDFor(player, WynnIds.HealthRegen.getId) 
      var max : Int = player.getMaxHealth
      var cur : Int = player.getHealth
      player.setHealth(Math.min(max,cur + (regen/10d).toInt + 1))
    }
        new PlayerHealthRegen().runTaskLater(MainClass.instance, 60)
  }
}

class PlayerManaRegen() extends BukkitRunnable(){
  final def run(){
    var online : Array[Player] = Bukkit.getOnlinePlayers()
    online.foreach { player => 
      var regen = IdentifiesHelper.getIDFor(player, WynnIds.ManaRegen.getId) 
      var max : Int = 20
      var cur : Int = player.getFoodLevel
      player.setFoodLevel(Math.min(max,cur + regen + 1))
    }
    new PlayerManaRegen().runTaskLater(MainClass.instance, 60)
  }
}

object DamageManager{
  

  
  
  final def damageAgainst(damage : Double  , defense : Array[Int]) : Double = {
    var dmg : Double = damage
    defense.toList.foreach { d => dmg = dmg * (1d-(d/100d)) }
    return dmg
  }
  
  final def getDefenseFor(player : Player) : Array[Int] = {
    var defense = Array[Int]()
    val inv = player.getInventory
    inv.getArmorContents.toList.foreach { armor =>
      if (armor!=null && armor.getType!=Material.AIR){
        val stack = CraftItemStack.asNMSCopy(armor)
        val tag = stack.getTag
        if (tag!=null && tag.hasKey("def")){
          defense = defense :+ tag.getInt("def")
        }
      }
    }
    return defense
  }
  
  final def getDamageFor(player : Player) : Int = {
    var damage = 0
    var multi = 1d
    val inv = player.getInventory
    inv.getArmorContents.toList.foreach { armor =>
      if (armor!=null && armor.getType!=Material.AIR){
        val stack = CraftItemStack.asNMSCopy(armor)
        val tag = stack.getTag
        if (tag!=null && tag.hasKey("Ids") && tag.getCompound("Ids").hasKey("dmg")){
          multi = multi + (multi * (tag.getCompound("Ids").getInt("dmg")/100d))
        }
      }
    }
    if (inv.getItemInHand!=null && inv.getItemInHand.getType!=Material.AIR){    
      val stack = CraftItemStack.asNMSCopy(inv.getItemInHand)
      val tag = stack.getTag
      if (tag!=null && tag.hasKey("Ids") && tag.getCompound("Ids").hasKey("dmg")){
        multi = multi + (multi * (tag.getCompound("Ids").getInt("dmg")/100d))
      }
      if (tag!=null && tag.hasKey("min_dmg")){
        damage = new Random().nextInt(tag.getInt("max_dmg")-tag.getInt("min_dmg"))+tag.getInt("min_dmg")
      }
    }
    return (damage * multi).toInt
  }
    
  
  final def damageBetween(vec1 : Vector , vec2 : Vector , world : World , damage : Double) : Unit = {
    var copy1 = vec1.clone()
    var copy2 = vec2.clone()
    world.getNearbyEntities(copy1.subtract(copy2.multiply(0.5d)).toLocation(world),vec2.getX,vec2.getY,vec2.getZ).foreach { ent => 
      if (ent.isInstanceOf[Player]){
        ent.asInstanceOf[Player].damage(DamageManager.damageAgainst(damage , getDefenseFor(ent.asInstanceOf[Player])))
      } else if (ent.isInstanceOf[LivingEntity]){
        ent.asInstanceOf[LivingEntity].damage(damage)
      } 
    }
  }
  
  final def damageNearby(loc : Location , x : Double , y : Double , z : Double, damage : Double) : Unit = {
    loc.getWorld.getNearbyEntities(loc,x,y,z).foreach { ent => 
      if (ent.isInstanceOf[Player]){
        ent.asInstanceOf[Player].damage(DamageManager.damageAgainst(damage , getDefenseFor(ent.asInstanceOf[Player])))
      } else if (ent.isInstanceOf[LivingEntity]){
        ent.asInstanceOf[LivingEntity].damage(damage)
      } 
    }
  }
  
    final def damageBetweenWithout(vec1 : Vector , vec2 : Vector , world : World , damage : Double , out : Entity) : Unit = {
    var copy1 = vec1.clone()
    var copy2 = vec2.clone()
    world.getNearbyEntities(copy1.subtract(copy2.multiply(0.5d)).toLocation(world),vec2.getX,vec2.getY,vec2.getZ).foreach { ent => 
      if (ent.getUniqueId.equals(out.getUniqueId)){
      } else if (ent.isInstanceOf[Player]){
        ent.asInstanceOf[Player].damage(DamageManager.damageAgainst(damage , getDefenseFor(ent.asInstanceOf[Player])))
      } else if (ent.isInstanceOf[LivingEntity]){
        ent.asInstanceOf[LivingEntity].damage(damage)
      } 
    }
  }
  
  final def damageNearbyWithout(loc : Location , x : Double , y : Double , z : Double, damage : Double , out : Entity) : Array[Entity] = {
    var ret = Array[Entity]() 
    loc.getWorld.getNearbyEntities(loc,x,y,z).foreach { ent => 
      if (ent.getUniqueId.equals(out.getUniqueId)){  
      }else if (ent.isInstanceOf[Player]){
        ent.asInstanceOf[Player].damage(DamageManager.damageAgainst(damage , getDefenseFor(ent.asInstanceOf[Player])))
      } else if (ent.isInstanceOf[LivingEntity]){
        ent.asInstanceOf[LivingEntity].damage(damage)
        ret = ret:+ent
      } 
    }
    return ret
  }
  final def moveNearbyWithout(loc : Location , x : Double , y : Double , z : Double, move : Vector , out : Entity) : Unit = {
    loc.getWorld.getNearbyEntities(loc,x,y,z).foreach { ent => 
      if (ent.getUniqueId.equals(out.getUniqueId)){
      } else if (ent.isInstanceOf[LivingEntity]){
        ent.asInstanceOf[LivingEntity].setVelocity(move)
      } 
    }
  }
  
}
