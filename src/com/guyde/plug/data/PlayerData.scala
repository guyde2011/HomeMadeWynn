package com.guyde.plug.data;

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.Random
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import net.minecraft.server.v1_8_R2.IChatBaseComponent.ChatSerializer
import net.minecraft.server.v1_8_R2.Item
import net.minecraft.server.v1_8_R2.NBTCompressedStreamTools
import net.minecraft.server.v1_8_R2.NBTTagCompound
import net.minecraft.server.v1_8_R2.PacketPlayOutChat
import com.guyde.plug.main.MainClass

class PlayerData(uuid : UUID){
  object Classes{
    val warrior=1;
    val mage=1;
    val archer=1;
    val assassin=1;
  }
  
}

object IdentifiesHelper{
  final def getIDFor(player : Player , str : String) : Int = {
    var lvl = 0
    val inv = player.getInventory
    inv.getArmorContents.toList.foreach { armor =>
      if (armor!=null && armor.getType!=Material.AIR){
        val stack = CraftItemStack.asNMSCopy(armor)
        val tag = stack.getTag
        if (tag!=null && tag.hasKey("Ids") && tag.getCompound("Ids").hasKey(str)){
          lvl = lvl + tag.getCompound("Ids").getInt(str)
        }
      }
    }
    if (inv.getItemInHand!=null && inv.getItemInHand.getType!=Material.AIR){    
      val stack = CraftItemStack.asNMSCopy(inv.getItemInHand)
      val tag = stack.getTag
      if (tag!=null && tag.hasKey("Ids") && tag.getCompound("Ids").hasKey(str)){
        lvl = lvl + tag.getCompound("Ids").getInt(str)
      }
    }
    return lvl
  }
  
  final def getXP(player : Player , min_exp : Int , max_exp : Int) : Int = {
    var xp_bonus = getIDFor(player,WynnIds.XpBonus.getId)
    var base_xp = new Random().nextInt(max_exp - min_exp) + min_exp
    return (base_xp+(xp_bonus*base_xp/100d)).toInt
  }
  
  final def getLootBonus(player : Player) : Int = {
    var loot_bonus = getIDFor(player,WynnIds.LootBonus.getId)
    return loot_bonus*25;
  }
  
  final def ManaPerHit(player : Player) : Int = {
   var mana_steal = getIDFor(player,WynnIds.ManaSteal.getId)
   return mana_steal
  }
  
}

class BowCooldown(player : Player) extends BukkitRunnable{
  override def run(){
    player.removeMetadata("bow_charge",MainClass.instance);
  }
}
object InventoryHelper {
  
  def serializeInv(inv : Inventory) : NBTTagCompound = {
    var comp = new NBTTagCompound();
    var items = new NBTTagCompound();
    comp.setInt("size", inv.getSize());
    comp.setInt("stackSize", inv.getMaxStackSize());
    comp.setString("name", inv.getName());
    comp.setString("title", inv.getTitle());
    var slot = 0;
    inv.getContents().foreach(stack=>{
      if (stack==null){
        var empty_slot = new NBTTagCompound();
        empty_slot.setBoolean("has_item", false);
        items.set(slot + "", empty_slot);
      } else {
        items.set(slot + "", serializeItem(stack));
      }
      slot=slot+1;
    })
    comp.set("items", items);
    return comp;
  }
  
  private def serializeItem(stack : ItemStack) : NBTTagCompound = {
    var itemStack = CraftItemStack.asNMSCopy(stack);
    var item_comp = if (itemStack.hasTag()) itemStack.getTag() else new NBTTagCompound();
    var item = new NBTTagCompound();
    item.setInt("id", Item.getId(itemStack.getItem()));
    item.setInt("meta", itemStack.getData());
    item.setInt("amount", stack.getAmount());
    item.set("nbt", item_comp);
    item.setBoolean("has_item", true);
    return item;
  }
  
  def unserializeInv(comp : NBTTagCompound) : Inventory = {
    var items = comp.getCompound("items");
    var size = comp.getInt("size");
    var stacks = new Array[ItemStack](size);
    for( slot <- 1 to size){
      var item = items.getCompound(slot + "");
      if (item.getBoolean("has_item")){
        stacks(slot) = unserializeItem(item);
      } else {
        stacks(slot) = null;
      }
    }   
    var inv = Bukkit.createInventory(null, 54)
    inv.setContents(stacks);
    return inv;
  }
  
  private def unserializeItem(item : NBTTagCompound) : ItemStack = {
    
    var id = item.getInt("id");
    var meta = item.getInt("meta");
    var amount = item.getInt("amount");
    var comp = item.getCompound("nbt");
    var itemStack = new net.minecraft.server.v1_8_R2.ItemStack(Item.getById(id),amount , meta);
    itemStack.setTag(comp);
    return CraftItemStack.asBukkitCopy(itemStack);
  }
}

trait InventorySaver{
  
  def getInventory() : Inventory;
  
  final def serializeString() : String = {
    var tag = InventoryHelper.serializeInv(getInventory);
    var stream = new ByteArrayOutputStream()
    NBTCompressedStreamTools.a(tag,stream)
    var bytes = stream.toByteArray()
    return new String(bytes,Charset.forName("UTF-8"))
  }
  
  final def serializeNBT() : NBTTagCompound = {
    return InventoryHelper.serializeInv(getInventory);
  }
  
  
}

object TextHelper{
  def aboveChatMessage(message : String , player : Player) : Unit ={
    var comp = ChatSerializer.a("{\"text\": \"" + message + "\"}")
    var packet = new PacketPlayOutChat(comp,2)
    var cp = player.asInstanceOf[CraftPlayer]
    cp.getHandle.playerConnection.sendPacket(packet);
  }
}

abstract class Skill(name : String , hunger1 : Int , hunger2 : Int , hunger3 : Int){
  val hunger = List[Int](hunger1,hunger2,hunger3)
  def run(level : Int , player : Player){
    if (hunger(level-1)>player.getFoodLevel){
      TextHelper.aboveChatMessage(ChatColor.DARK_RED + "You don't have enough hunger to cast this spell!", player)
    } else {
      TextHelper.aboveChatMessage(ChatColor.GOLD + name + " spell cast " + ChatColor.GRAY + "-" + hunger(level-1) + " Hunger!",player)
      player.setFoodLevel(player.getFoodLevel-hunger(level-1))
      runSkill(level,player)
    }
  }
  
  def runSkill(level : Int , player : Player)
}


object DropRates{
  private var drop_rates = Map[Int,Array[DropRate]]()
  
  final def registerMisc(level : Int, item : WynnItem , rate : Int){
    var array = Array[DropRate]()
    if(drop_rates.contains(level)){
      array = drop_rates(level)
    }
    array = array:+new DropRate(item , rate)
    drop_rates = drop_rates + (level -> array)
  }
  
  
  final def registerTier(item : TierableItem){
    var level = item.level
    var array = Array[DropRate]()
    if(drop_rates.contains(level)){
      array = drop_rates(level)
    }
    array = array:+new DropRate(item)
    drop_rates = drop_rates + (level -> array)
  }
  
  final def getDropRateRange(lvl : Int) : Array[DropRate] = {
    val min : Int = Math.max(lvl-10,0)
    val max : Int = Math.min(lvl+10,100) 
    var array = Array[DropRate]()
    for (i <- min to max){
      if (drop_rates.contains(i)){
        array = array++drop_rates(i)
      }
    }
    return array
  }
  
}

class DropRate(val item : WynnItem ,val rate : Int){
  def this(t_item : TierableItem){
    this(t_item , (4 - t_item.tier) * (6 - t_item.tier) - 1)
  }
}

abstract class GameClass(uuid : UUID, click : Click, weapon : Material) extends InventorySaver{
  var level_requirement : Int
  var name : String
  var chatname : String
  var level = 0;
  var getWeapon = weapon
  
  def getClick() : Click = click;
  
  def getInventory() : Inventory = {
    return Bukkit.getPlayer(uuid).getInventory;
  }
  
  def First_Play(){
    level = 1;
  }
  
  def JoinMessage(server : String) : String = {
    return ChatColor.DARK_GREEN + Bukkit.getPlayer(uuid).getDisplayName + " has logged-in to " + server + " as a " + name 
  }
  
  def first_skill : Skill
  
  def second_skill : Skill
  
  def third_skill : Skill
  
  def fourth_skill : Skill
  
  
}


class Click(time : Long , ID : Int, name : String){
  def Time = time
  def id=ID
  def Name = name
  override def equals(obj : Any) : Boolean = {return obj.isInstanceOf[Click] && obj.asInstanceOf[Click].id==this.id}
}

object Clicks{
  final def Right = new Click(System.currentTimeMillis(),0,"Right");
  final def Left = new Click(System.currentTimeMillis(),1,"Left");
}
object PlayerDataManager{
  private var classFor : Map[UUID,GameClass] = Map[UUID,GameClass]();
  private var clickFor : Map[UUID,Array[Click]] = Map[UUID,Array[Click]]();
  
  final def setClass(p : Player , c : GameClass)  {
    classFor = classFor + (p.getUniqueId-> c)
  }
  
  final def GetClass(p : Player) : GameClass = {
    return classFor(p.getUniqueId)
  }
  
    final def setClicks(p : Player , c : Array[Click])  {
    clickFor = clickFor + (p.getUniqueId-> c)
  }
  
  final def getClicks(p : Player) : Array[Click] = {
    return clickFor(p.getUniqueId)
  }
  
  final def runSkill(click1 : Click , click2 : Click , click3 : Click , p : Player){
    if (click2.equals(click1) && click2.equals(click3)){
      PlayerDataManager.GetClass(p).second_skill.run(1, p);
    }
    if (!click2.equals(click1) && !click2.equals(click3)){
      PlayerDataManager.GetClass(p).first_skill.run(1, p);
    }
    if (!click2.equals(click1) && click2.equals(click3)){
      PlayerDataManager.GetClass(p).third_skill.run(3, p);
    }
    if (click2.equals(click1) && !click2.equals(click3)){
      PlayerDataManager.GetClass(p).fourth_skill.run(1, p);
    }
  }
  final def Click(p : Player , click : Click){
    if (!clickFor.keySet.contains(p.getUniqueId)){
      setClicks(p,null)
    }
    var clicks = clickFor(p.getUniqueId)
    if (clicks==null){
      if (!GetClass(p).getClick().equals(click)) return
      setClicks(p,Array[Click](click))
      TextHelper.aboveChatMessage(click.Name, p)
      return
    }
    if (clicks.length==0){
      if (!GetClass(p).getClick().equals(click)) return
      setClicks(p,Array[Click](click))
      TextHelper.aboveChatMessage(click.Name, p)
      return
    }
    var last_click = clicks(clicks.length-1)
    if (click.Time-last_click.Time>1500){
      setClicks(p,Array[Click](click)) 
      TextHelper.aboveChatMessage(click.Name, p)
      return
    }
    if (clicks.length==1){
      setClicks(p,Array[Click](clicks(0),click))
      TextHelper.aboveChatMessage(clicks(0).Name + " - " + click.Name, p)
    }
    if (clicks.length==2){
      setClicks(p,Array[Click]())
      TextHelper.aboveChatMessage(clicks(0).Name + " - " + clicks(1).Name + " - " + click.Name, p)
      runSkill(clicks(0),clicks(1),click,p)
    }
  }

  
  
  
  
}


  
  
  
