package com.guyde.plug.data;

import scala.util.control.Breaks._

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager.Profession
import org.bukkit.util.Vector
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

import com.guyde.plug.data.MobEquipment
class RegisterWeapon{
  def load(obj : LuaValue) : LuaValue = {
     if (obj.istable()){
       var table = obj.asInstanceOf[LuaTable] 
       var min_dmg = table.get("min_dmg").toint
       var max_dmg = table.get("max_dmg").toint
       var name = table.get("name").tojstring()
       var lvl = table.get("lvl").toint()
       var item = Material.getMaterial(table.get("item").tojstring())
       var tier = table.get("tier").toint()
       var wynn = new WynnWeapon(name,item,min_dmg,max_dmg,lvl,tier)
       var k = LuaValue.NIL
       var ids = Map[String , Int]()
       breakable {while (true) {
         var n = table.get("ids").asInstanceOf[LuaTable].next(k)
         k=n.arg1()
         if (k.isnil()){
           break
         }
         var id = n.arg(2)
         ids = ids + (id.get("name").tojstring() -> id.get("lvl").toint())
       }}
       wynn.Ids = ids
       wynn.register()
     }
     
    
    return LuaValue.NIL
  }
  
}

class RegisterArmor{
  def load(obj : LuaValue) : LuaValue = {
     if (obj.istable()){
       var table = obj.asInstanceOf[LuaTable] 
       var defense = table.get("def").toint

       var name = table.get("name").tojstring()
       var lvl = table.get("lvl").toint()
       var item = Material.getMaterial(table.get("item").tojstring())
       var tier = table.get("tier").toint()

       var wynn = new WynnArmor(name,item,defense,lvl,tier)
       var k = LuaValue.NIL
       var ids = Map[String , Int]()
       breakable {while (true) {
         var n = table.get("ids").asInstanceOf[LuaTable].next(k)
         k=n.arg1()
         if (k.isnil()){
           break
         }
         var id = n.arg(2)
         ids = ids + (id.get("name").tojstring() -> id.get("lvl").toint())
       }}
       wynn.Ids = ids
       if (table.get("color")!=null){
         var color = table.get("color").toint
       }

       wynn.register()
       
     }
     
    
    return LuaValue.NIL
  }
  
}

class RegisterAggro{
  def load(obj : LuaValue) : LuaValue = {
     if (obj.istable()){
       var table = obj.asInstanceOf[LuaTable] 
       val hp = table.get("health").toint

       val name = table.get("name").tojstring()
       val lvl = table.get("lvl").toint()
       val e_type = EntityType.fromName(table.get("type").tojstring())
       var holder = new MetaHolder;
       val max_dmg = table.get("max_dmg").todouble
       val min_dmg = table.get("min_dmg").todouble
       val min_exp = table.get("min_exp").toint
       val max_exp = table.get("max_exp").toint
       val unlocal_name = table.get("unlocal_name").tojstring
       val canAttack = table.get("canAttack").toboolean
       var equip : LuaTable = null
       if (table.get("equipment")!=null && !table.get("equipment").equals(LuaValue.NIL)){
          equip = table.get("equipment").asInstanceOf[LuaTable];
          holder.put("equipment", new MobEquipment() + equip.get("helmet") + equip.get("chestplate") + equip.get("leggings") + equip.get("boots") ++ equip.get("held"))
       }
       var drops = Array[MobDrop]()
       if (table.get("drops")!=LuaValue.NIL){
       var p = new LTable(table.get("drops").asInstanceOf[LuaTable])
       p.foreachInt{
         e =>
           val cur = new LTable(e._2.asInstanceOf[LuaTable])
           val pos = cur.getVector("pos")
           val rate = cur.getInt("rate")
           val slot = cur.getInt("slot")
           drops = drops:+new MobDrop(pos,slot,rate)
       }
       
       }
       holder.put("drops", drops)
       var wynn = new WynnAggressive(hp,min_dmg,max_dmg,min_exp,max_exp,lvl,name,unlocal_name,canAttack,holder,e_type)
       wynn.register
     }
     
    
    return LuaValue.NIL
  }
  
}
class LTable(val table : LuaTable){
  final def getDouble(str : String) : Double = table.get(str).todouble()

  final def getInt(str : String) : Int = table.get(str).toint()
  
  final def getString(str : String) : String = table.get(str).tojstring()
  
  final def getTable(str : String) : LTable = new LTable(table.get(str).asInstanceOf[LuaTable])
 
  final def getVector(str : String) : Vector = {
    val table1 = getTable(str)
    return new Vector(table1.getDouble("x"),table1.getDouble("y"),table1.getDouble("z"))
  }
  final def foreach(e : ((LuaValue, LuaValue)) => Unit){
    var k = LuaValue.NIL
    breakable {
      while (true) {
        var n = table.next(k)
        k=n.arg1()
        if (k.isnil()){
          break
        }
        e.apply(k,n.arg(2))
      }
    }
  }
  
  final def foreachInt(e : ((Int, LuaValue)) => Unit){
    var k = LuaValue.NIL
    breakable {
      while (true) {
        var n = table.next(k)
        k=n.arg1()
        if (k.isnil()){
          break
        }
        e.apply(k.toint(),n.arg(2))
      }
    }
  }
  
}
class RegisterQuestNPC{
  private final def getProfession(str : String) : Profession = {
    str match {
      case "PINK_ROBE" => return Profession.PRIEST
      case "BLACK_OVERALL" => return Profession.BLACKSMITH
      case "WHITE_OVERALL" => return Profession.BUTCHER
      case "BROWN_ROBE" => return Profession.FARMER
      case "WHITE_ROBE" => return Profession.LIBRARIAN
    }
    return null
  }

  def load(obj : LuaValue) : LuaValue = {
    if (obj.istable()){
       val table = new LTable(obj.asInstanceOf[LuaTable])
       val pos = new Vector(table.getDouble("x"),table.getDouble("y"),table.getDouble("z"))
       val name = table.getString("name")
       val unlocal_name = table.getString("unlocal_name")
       var after = Map[String,QuestMessage]()
       table.getTable("after_speech").foreach{ e =>
         after = after + (e._1.tojstring->new QuestMessage(e._2.tojstring)) 
       }
       val npc = new QuestNPC(pos,name,unlocal_name,after,getProfession(table.getString("profession")))
       Quests.registerNPC(npc)
    }
    return LuaValue.NIL
  }
}

class RegisterMobRegion{
  def load(obj : LuaValue) : LuaValue = {
    val table = new LTable(obj.asInstanceOf[LuaTable])
    val vec1 = table.getVector("start")
    val vec2 = table.getVector("bound")
    var mobs = Array[String]()
    table.getTable("mobs").foreachInt{e =>
      mobs = mobs :+ e._2.tojstring()
    }
    new MobRegion(table.getInt("rate"),mobs,vec1,vec2)
    return LuaValue.NIL
  }
}

class RegisterQuest{
  def load(obj : LuaValue) : LuaValue = {
    val table = new LTable(obj.asInstanceOf[LuaTable])
    val name = table.getString("name")
    val unlocal_name = table.getString("unlocal_name")
    val level = table.getInt("lvl")
    val stages = table.getTable("stages")
    var quest_stages = Array[AbstractQuestStage]()
    stages.foreachInt{e =>
      val stage = new LTable(e._2.asInstanceOf[LuaTable])
      val kind = stage.getString("stage_type")
      kind match {
        case "talk" =>{
          var msgs = Array[QuestMessage]()
          stage.getTable("dialogue").foreachInt{talk =>
            val msg = talk._2.tojstring()
            msgs = msgs:+new QuestMessage(msg)
          }
          val npc_name = stage.getString("npc")
          val text = new QuestDialogue(msgs,Quests.getNPC(npc_name))
          quest_stages = quest_stages :+ new TalkQuestStage(stage.getString("book"),Quests.getNPC(npc_name),text)
        }
        
        case "talk_item" =>{
          var msgs = Array[QuestMessage]()
          stage.getTable("dialogue").foreachInt{talk =>
            val msg = talk._2.tojstring()
            msgs = msgs:+new QuestMessage(msg)
          }
          val npc_name = stage.getString("npc")
          val text = new QuestDialogue(msgs,Quests.getNPC(npc_name))
          val vec = stage.getVector("pos")
          val slot = stage.getInt("slot")
          quest_stages = quest_stages :+ new TalkItemQuestStage(stage.getString("book"),Quests.getNPC(npc_name),text,vec,slot)
        }
        case "region" =>{}
      }
    }
    val quest = new Quest(level,name,unlocal_name,quest_stages)
    return LuaValue.NIL
  }

}