package com.guyde.plug.data

import java.util.UUID
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.bukkit.entity.Zombie
import org.bukkit.entity.Villager
import org.bukkit.entity.Villager.Profession
import net.minecraft.server.v1_8_R2.NBTTagCompound
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftVillager
import com.guyde.plug.utils.TextCreator
import com.guyde.plug.utils.MCFormat
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import scala.collection.JavaConversions._
import com.guyde.plug.main.MainClass
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.block.Chest

class QuestIdentifier(val id : Int){
  override def hashCode() : Int = {
    return id
  }
}

object Quests{
  var kill = Array[Entity]()
  private var count = 0
  private var quests = Map[QuestIdentifier,Quest]()
  private var quests_unloc = Map[String,QuestIdentifier]()
  private var quest_npcs = Map[String,QuestNPC]()
  
  
  final def getNPCs() : java.util.Map[String,QuestNPC] = {
    return quest_npcs
  }
  
  final def reset(){
    kill = Array[Entity]()
    count = 0
    quests = Map[QuestIdentifier,Quest]()
    quests_unloc = Map[String,QuestIdentifier]()
    quest_npcs = Map[String,QuestNPC]()
    PlayerDataManager.resetQuests()
  }
  
  final def getNPCsScala() : Map[String,QuestNPC] = {
    return quest_npcs
  }
  
  final def foreach(e : ((String))=>Unit){
    quests_unloc.foreach(p => e.apply(p._1))
  }
  final def registerNPC(npc : QuestNPC){
    quest_npcs = quest_npcs + (npc.unlocal_name -> npc)
  }
  
  final def getNPC(str : String) : QuestNPC = {
    return quest_npcs(str)
  }
  
  final def getNewIdentifier(quest : Quest) : QuestIdentifier = {
    count = count + 1;
    val id = new QuestIdentifier(count-1)
    quests = quests + (id -> quest)
    quests_unloc = quests_unloc + (quest.unlocal_name -> id)
    return id
  }
  
  final def getQuest(unlocal : String) : Quest = {
    return quests(quests_unloc(unlocal))
  }
  
  final def NpcClicked(event : PlayerInteractEntityEvent){
    val unlocal = event.getRightClicked.getMetadata(QuestConstants.UNLOCAL_NAME).get(0).asString()
    val npc = getNPC(unlocal)
    event.setCancelled(true)
    var quest_talk = Array[TalkQuestStage]()
    var quests_item = Array[Quest]()
    var quest_talk_item = Array[TalkItemQuestStage]()
    var quests = Array[Quest]()
    Quests.foreach { name => 
      var status = PlayerDataManager.getQuestStatus(event.getPlayer, name)
      var quest = Quests.getQuest(name)
      if (status.started!=2){
        var stage = quest.stages(status.getStage())
        if (stage.isInstanceOf[TalkQuestStage] && stage.asInstanceOf[TalkQuestStage].npc.unlocal_name.equals(unlocal)){
          quest_talk = quest_talk:+stage.asInstanceOf[TalkQuestStage]
          quests = quests :+ quest
        }
      }
    }
    
    Quests.foreach { name => 
      var status = PlayerDataManager.getQuestStatus(event.getPlayer, name)
      var quest = Quests.getQuest(name)
      if (status.started!=2){
        var stage = quest.stages(status.getStage())
        if (stage.isInstanceOf[TalkItemQuestStage] && stage.asInstanceOf[TalkItemQuestStage].npc.unlocal_name.equals(unlocal) && event.getPlayer.getInventory.contains(stage.asInstanceOf[TalkItemQuestStage].getStack())){
          quest_talk_item = quest_talk_item:+stage.asInstanceOf[TalkItemQuestStage]
          quests_item = quests_item :+ quest
        }
      }
    }
    if (quest_talk.length>0){
      var printer = quest_talk(0).text.createPrinter(event.getPlayer)
      var finish = quests(0).stages.length-1<=PlayerDataManager.getQuestStatus(event.getPlayer, quests(0).unlocal_name).getStage()
      for (i <- 1 to quest_talk(0).text.messages.length+1){
        new PrinterTask(PlayerDataManager.getQuestStatus(event.getPlayer, quests(0).unlocal_name),printer,i*60-60,finish,quests(0))
      }
    } else if (quest_talk_item.length>0){
      var printer = quest_talk(0).text.createPrinter(event.getPlayer)
      var finish = quests(0).stages.length-1<=PlayerDataManager.getQuestStatus(event.getPlayer, quests(0).unlocal_name).getStage()
      for (i <- 1 to quest_talk(0).text.messages.length+1){
        new PrinterTask(PlayerDataManager.getQuestStatus(event.getPlayer, quests(0).unlocal_name),printer,i*60-60,finish,quests(0))
      }
    } else{
      val seq = npc.after_speech.toSeq.filter{
        x => PlayerDataManager.getQuestStatus(event.getPlayer,x._1).started==2
      }
      if (seq.length>0){
        event.getPlayer.sendMessage(seq(0)._2.text.replace("<n>", ChatColor.DARK_GREEN + npc.name + ":" + " " + ChatColor.GREEN))
      } else {
        event.getPlayer.sendMessage(ChatColor.DARK_GREEN + npc.name + ":" + " " + ChatColor.GREEN + "Hello " + event.getPlayer.getName + "!")
      }
    }
    
  }
}

class PrinterTask(quest : QuestStatus, printer : AbstractPrinter, delay : Int , finish : Boolean , que : Quest) extends BukkitRunnable(){
  this.runTaskLater(MainClass.instance, delay)
  final def run(){
    if (!printer.next()){
      finish match{
        case false => if (quest.started==0) {quest.Start()} else {quest.nextStage()}
        case true => quest.Finish(que)
      }
    }
  }
}

abstract class AbstractPrinter(){
  def next() : Boolean
}

class Quest(val level : Int , val name : String , val unlocal_name : String ,val stages : Array[AbstractQuestStage]){
  val identifier : QuestIdentifier = Quests.getNewIdentifier(this) 
  
  override def hashCode() : Int = {
    return identifier.hashCode()
  }
  
  
}

abstract class AbstractQuestStage(val book_text : String){
}

class TalkQuestStage(book_text : String , val npc : QuestNPC , val text : QuestDialogue) extends AbstractQuestStage(book_text){
  
}


class TalkItemQuestStage(book_text : String , val npc : QuestNPC , val text : QuestDialogue , cords : Vector , slot : Int) extends AbstractQuestStage(book_text){
  final def getStack() : ItemStack = {
    return MainClass.world.getBlockAt(cords.toLocation(MainClass.world)).getState.asInstanceOf[Chest].getInventory.getItem(slot)
  }
}
class RegionQuestStage(book_text : String , start : Vector , end : Vector) extends AbstractQuestStage(book_text){
  

  
}

class QuestDialogue(val messages : Array[QuestMessage] , val npc : QuestNPC){
  val numbered_count : Int = getNumberedCount()

  private def getNumberedCount() : Int = {
    return messages.filter{ msg => msg.isNumbered() }.length
  }
  
  final def createPrinter(player : Player) : Printer = {
    return new Printer(player)
  }
  
  class Printer(player : Player) extends AbstractPrinter{
    private var index : Int = 0; 
    private var count_index : Int = 0
    
    final def next() : Boolean = {
      if (index>=messages.length) return false
      if (messages(index).isNumbered()) count_index = count_index + 1
      val final_msg = messages(index).text.replace("<c>", ChatColor.GRAY + "[" + count_index + "/" + numbered_count + "]").replace("<n>", ChatColor.DARK_GREEN + npc.name + ":" + " " + ChatColor.GREEN)
      player.sendMessage(final_msg.replace("&", "§"))
      index = index + 1
      return true
    }
    
  }
}

class QuestMessage(val text : String){
  final def isNumbered() : Boolean = {
    return text.contains("<c>")
  }
  
  final def isNamed() : Boolean = {
    return text.contains("<n>")
  }
}

object QuestConstants{
  val QUEST_NPC = "QuestNPC"
  val UNLOCAL_NAME = "Unlocal_Name"
  val PINK_ROBE = Profession.PRIEST
  val BLACK_OVERALL = Profession.BLACKSMITH
  val WHITE_OVERALL = Profession.BUTCHER
  val BROWN_ROBE = Profession.FARMER
  val WHITE_ROBE = Profession.LIBRARIAN
}
class QuestNPC(val position : Vector , val name : String , val unlocal_name : String ,val after_speech : Map[String,QuestMessage] ,prof : Profession){
  final def Spawn(world : World){
    var holder = new MetaHolder()
    holder.putBoolean(QuestConstants.QUEST_NPC, true)
    holder.put(QuestConstants.UNLOCAL_NAME, unlocal_name)
    var spawned = world.spawn(position.toLocation(world), classOf[Villager])
    holder.attachTo(spawned)
    spawned.setAdult()
    spawned.setProfession(prof)
    var nms = spawned.asInstanceOf[CraftVillager].getHandle
    var comp = nms.getNBTTag
    if (comp==null){
      comp = new NBTTagCompound();
    }
    nms.c(comp)
    comp.setInt("NoAI", 1)
    nms.f(comp)
    TextCreator.createTrackedText(ChatColor.DARK_GREEN + name, spawned, new Vector(0,0.125,0))
    TextCreator.createTrackedText(ChatColor.GRAY + "Quest NPC", spawned, new Vector(0,-0.125,0))
    Quests.kill = Quests.kill :+ spawned
  }
}

class QuestBook(){
  
}

class QuestPage(){
  
}

class QuestStatus(val uuid : UUID){
  private var stage = 0
  var started = 0
  private val player = Bukkit.getPlayer(uuid)
  
  def getStage() : Int = stage
  
  def Start(){
    stage = 1
    started = 1
  }
  
  def nextStage(){
    stage = stage+1
  }
  
  def Finish(quest : Quest){
    started = 2
    stage = stage+1
  }
  
}

class QuestTracker(uuid : UUID){
  
}


object QuestManager{
  
}