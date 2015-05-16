package com.guyde.plug.data;

import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.LuaValue
import org.luaj.vm2.LuaTable
import org.bukkit.Material
import scala.util.control.Breaks._
import org.bukkit.entity.EntityType
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

       var wynn = new WynnAggressive(hp,min_dmg,max_dmg,min_exp,max_exp,lvl,name,unlocal_name,canAttack,holder,e_type)
       wynn.register
     }
     
    
    return LuaValue.NIL
  }
  
}