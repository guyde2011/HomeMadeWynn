package com.guyde.plug.data;

import java.io.File
import com.guyde.plug.utils.Conversions._
import java.util.Random
import java.util.UUID
import scala.collection.JavaConversions._
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.scheduler.BukkitRunnable
import com.guyde.plug.main.MainClass
import net.minecraft.server.v1_8_R2.IChatBaseComponent.ChatSerializer
import net.minecraft.server.v1_8_R2.NBTTagCompound
import net.minecraft.server.v1_8_R2.PacketPlayOutChat
import org.bukkit.Sound
class PlayerData(uuid : UUID){
  object Classes{
    
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

abstract class SerializedData[T](private var _value : T){
  final def value = _value
  def put(file : FileConfiguration , path : String) : Unit
  protected def read(file : FileConfiguration , path : String) : T
  final def readFrom(file : FileConfiguration , path : String) : T = {
    _value = read(file , path)
    return _value
  }
  
}

class DemaPlayerInv(val inv : Array[ItemStack], val armor : Array[ItemStack]){
  final def setPlayerInventory(player : Player){
    player.getInventory.setContents(inv)
    player.getInventory.setArmorContents(armor)
  }
  
  def this(player : Player){
    this(player.getInventory.getContents,player.getInventory.getArmorContents)
  }
}



class SerializedInventory(private var inv : DemaPlayerInv) extends SerializedData[DemaPlayerInv](inv){
  def this() = this(null)
  
  
  override def read(file : FileConfiguration , path : String) : DemaPlayerInv = {
    var stacks = Array[ItemStack]()
    for (i <- 1 to 36){
      stacks = stacks :+ file.getItemStack(path + ".slot"+i)
    }
    var armor = Array[ItemStack](file.getItemStack(path+".helmet"),file.getItemStack(path+".chestplate"),file.getItemStack(path+".leggings"),file.getItemStack(path+".boots")) 
    inv = new DemaPlayerInv(stacks,armor)
    return inv
  }
  
  final def put(file : FileConfiguration , path : String){
    for (i <- 1 to 36){
      file.set(path + ".slot"+i , inv.inv(i-1))
    }
    file.set(path+".helmet",inv.armor(0))
    file.set(path+".chestplate",inv.armor(1))
    file.set(path+".leggings",inv.armor(2))
    file.set(path+".boots",inv.armor(3))
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
      PlayerDataManager.failSound(player)
    } else {
      TextHelper.aboveChatMessage(ChatColor.GOLD + name + " spell cast " + ChatColor.GRAY + "-" + hunger(level-1) + " Hunger!",player)
      player.setFoodLevel(player.getFoodLevel-hunger(level-1))
      PlayerDataManager.skillSound(player)
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

abstract class GameClass(uuid : UUID, click : Click, weapon : Material){
  var level_requirement : Int
  var name : String
  var chatname : String
  var level = 1
  var getWeapon = weapon
  private var xp = 0
  
  def getClick() : Click = click
  
  def addEXP(exp : Int){
    if (level>=75){
      return
    }
    xp = exp+xp 
    while (xp>=getXpFor(level)){
      xp = xp - getXpFor(level)
      level_up()
    }
    owner.setLevel(level)
    owner.setExp(xp.toFloat/getXpFor(level).toFloat)
  }
  
  def level_up(){
    owner.getNearbyEntities(50, 50, 50).foreach { 
      x =>
      x.sendMessage(ChatColor.RED + owner.getDisplayName() + " is now level " + (level+1)) 
    }
    level = level + 1
    owner.sendMessage("                              " + ChatColor.YELLOW + ChatColor.BOLD + "Level Up")
  }
  
  def owner : Player = Bukkit.getPlayer(uuid)
  
  def read(file : FileConfiguration , path : String){
    val inv = new SerializedInventory().readFrom(file, path + ".inv")
    inv.setPlayerInventory(owner)
    level = file.getInt(path + ".lvl")
    xp = file.getInt(path + ".exp")
    var quest_status = Map[String,QuestStatus]()
    val section = file.getConfigurationSection(path + ".quests")
    section.getKeys(false).foreach { key => 
      val stat = new QuestStatus(uuid) 
      val cur = section.getConfigurationSection(key)
      stat.started=cur.getInt("started")
      stat.setStage(cur.getInt("stage"))
      quest_status = quest_status + (key -> stat) 
    }
    owner.setLevel(level)
    owner.setExp(xp.toFloat/getXpFor(level).toFloat)
    PlayerDataManager.setQuestStatus(owner,quest_status)
  }
  
  def write(file : FileConfiguration , path : String){
    val inv = new DemaPlayerInv(owner)
    new SerializedInventory(inv).put(file, path + ".inv")
    file.set(path + ".lvl",level)
    file.set(path + ".exp",xp)  
    file.set(path + ".quests.dummy_quest.started",0)
    file.set(path + ".quests.dummy_quest.stage",0)
    PlayerDataManager.getQuestStatus(owner).foreach { e => 
      file.set(path + ".quests." + e._1 + ".started",e._2.started)
      file.set(path + ".quests." + e._1 + ".stage",e._2.getStage())
    }

    file.save(new File(MainClass.instance.getDataFolder,owner.getUniqueId + "/data.yml"))
  }
  
  def getXpFor(lvl : Int) : Int = lvl * lvl * lvl + lvl * lvl * 75 
  
  def First_Play(){
    level = 1;
    xp = 0
    val inv = owner.getInventory
    inv.clear()
    inv.setArmorContents(Array[ItemStack](null,null,null,null))
    inv.setItem(8, PlayerDataManager.SoulPoint(15))
    inv.setItem(7, new QuestBook(uuid).createBook())
  }
  
  def JoinMessage(server : String) : String = {
    return ChatColor.DARK_GREEN + owner.getDisplayName + " has logged-in to " + server + " as a " + name 
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
  private val soul_point = new ItemStack(Material.NETHER_STAR);
  private val meta = soul_point.getItemMeta()
  meta.setDisplayName("§bSoul Point")
  soul_point.setItemMeta(meta)
  def SoulPoint(amount : Int) : ItemStack = {val sp1 = soul_point.clone(); sp1.setAmount(amount); return sp1} 
  private var classFor : Map[UUID,GameClass] = Map[UUID,GameClass]();
  private var clickFor : Map[UUID,Array[Click]] = Map[UUID,Array[Click]]();
  private var questStatus : scala.collection.mutable.Map[UUID,Map[String,QuestStatus]] = scala.collection.mutable.Map[UUID,Map[String,QuestStatus]]()
  
  final def setQuestStatus(player : Player , map : Map[String,QuestStatus]) = {
    questStatus(player.getUniqueId) = map
  }
  final def resetQuests(){
 //   questStatus = scala.collection.mutable.Map[UUID,Map[String,QuestStatus]]()
  }
  
  final def getPlayerFile(p : Player) : FileConfiguration = {
    if (!new File(MainClass.instance.getDataFolder,p.getUniqueId + "/data.yml").exists()){
      new File(MainClass.instance.getDataFolder,p.getUniqueId.toString()).mkdirs()
      new File(MainClass.instance.getDataFolder,p.getUniqueId.toString() + "/data.yml").createNewFile()
    }
    var config = YamlConfiguration.loadConfiguration(new File(MainClass.instance.getDataFolder,p.getUniqueId + "/data.yml"))
    return config
  }
  final def setClass(p : Player , c : GameClass)  {
    if (classFor.contains(p.getUniqueId)){
      classFor(p.getUniqueId).write(getPlayerFile(p), classFor(p.getUniqueId).name)
    }
    if (getPlayerFile(p).contains(c.name)){
      c.read(getPlayerFile(p), c.name)
      classFor = classFor + (p.getUniqueId-> c)
    } else {
      classFor = classFor + (p.getUniqueId-> c)
      c.First_Play()
      classFor = classFor + (p.getUniqueId-> c)
    }

  }
  
  final def getQuestStatus(player : Player , str : String) : QuestStatus = {
    if (!questStatus.contains(player.getUniqueId)){
      var status = new QuestStatus(player.getUniqueId)
      questStatus = questStatus + (player.getUniqueId -> Map[String,QuestStatus](str->status))
      return status
    }
    var statuses : Map[String,QuestStatus] = questStatus(player.getUniqueId)
    if (!statuses.contains(str)){
      statuses = statuses+(str -> new QuestStatus(player.getUniqueId))
      questStatus(player.getUniqueId) = statuses
    }
    return questStatus(player.getUniqueId)(str)
    
  }
  
  final def getQuestStatus(player : Player) : Map[String,QuestStatus] = {
    if (!questStatus.contains(player.getUniqueId)){
      questStatus = questStatus + (player.getUniqueId -> Map[String,QuestStatus]())
    }
    return questStatus(player.getUniqueId)
    
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
  
  final def skillSound(p : Player){
    p.playSound(p.getLocation, Sound.ORB_PICKUP, 0.5f, 0.5f)
  }
  
  final def failSound(p : Player){
    p.playSound(p.getLocation, Sound.ANVIL_LAND, 1f, 1f)
  }
  
  final def clickR(p : Player){
    p.playSound(p.getLocation, Sound.CLICK, 0.5f, 1)
  }
  
  final def clickL(p : Player){
    p.playSound(p.getLocation, Sound.CLICK, 1, 1)
  }
  
  final def runSkill(click1 : Click , click2 : Click , click3 : Click , p : Player){
    
    if (click2.equals(click1) && click2.equals(click3) && GetClass(p).level>=11){
      GetClass(p).level match {
        case x if 46>x && x>=26 => p.second_skill.run(2, p)
        case y if y>=46 => p.second_skill.run(3, p)
        case z => p.second_skill.run(1, p) 
      }
    }
    if (!click2.equals(click1) && !click2.equals(click3)){
      GetClass(p).level match {
        case x if 36>x && x>=16 => p.first_skill.run(2, p); return
        case y if y>=36 => p.first_skill.run(3, p); return
        case z => p.first_skill.run(1, p); return
      }
    }
    if (!click2.equals(click1) && click2.equals(click3) && GetClass(p).level>=21){
      GetClass(p).level match {
        case x if 56>x && x>=36 => p.third_skill.run(2, p); return
        case y if y>=56 => p.third_skill.run(3, p); return
        case z => p.third_skill.run(1, p); return
      }
    }
    if (click2.equals(click1) && !click2.equals(click3) && GetClass(p).level>=31){
      GetClass(p).level match {
        case x if 66>x && x>=46 => p.fourth_skill.run(2, p); return
        case y if y>=66 => p.fourth_skill.run(3, p); return
        case z => p.fourth_skill.run(1, p); return
      }
    }
  }
  final def Click(p : Player , click : Click){
    val player = p
    val inv = player.getInventory
    if (!(inv.getItemInHand!=null && inv.getItemInHand.getType!=Material.AIR && inv.getItemInHand.getType==player.getWeapon)) return
    
    val stack = CraftItemStack.asNMSCopy(inv.getItemInHand)
    val tag = stack.getTag
    if (tag.hasKey("lvl") && tag.getInt("lvl")>player.level){
      return
    }
    if (!clickFor.keySet.contains(p.getUniqueId)){
      setClicks(p,null)
    }
    var clicks = clickFor(p.getUniqueId)
    if (GetClass(p).getClick().equals(click)){
      clickR(p)
    } else if(clicks!=null && clicks.length!=0 && clicks(clicks.length-1).Time-click.Time<=1500){
      clickL(p)
    }
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


  
  
  
