package com.guyde.plug.data

import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftVillager
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack
import org.bukkit.entity.Villager
import org.bukkit.entity.Villager.Profession
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.luaj.vm2.LuaTable
import com.guyde.plug.utils.TextCreator
import net.minecraft.server.v1_8_R2.MerchantRecipe
import net.minecraft.server.v1_8_R2.MerchantRecipeList
import net.minecraft.server.v1_8_R2.NBTTagCompound
import com.guyde.plug.main.MainClass
import org.bukkit.block.Chest
import org.bukkit.Material
import org.luaj.vm2.LuaValue



class Trades(){
  type Trade = (ItemStack,ItemStack,ItemStack)
  def offers = trades
  private var trades = Array[Trade]()
  implicit def toTrade(s1 : ItemStack, s2 : ItemStack, s3 : ItemStack) : Trade = new Trade(s1,s2,s3)
  def +(trade : Trade) : Trades = {
    trades = trades :+ trade
    return this
  }
  
  def getRecipeList : MerchantRecipeList = {
    var list = new MerchantRecipeList
    trades.foreach{trade=>
      val offer = new MerchantRecipe(trade._1,trade._2,trade._3)
      list.add(offer)
    }
    return list
  }
 
  def getOffers() = offers
  
  private def newTrade(table : LTable) : Trade = {
    val item1 = table.getTable("item1")
    val vector1 = item1.getVector("pos")
    val slot1 = item1.getInt("slot")
    val block1 = MainClass.world.getBlockAt(vector1.toLocation(MainClass.world))
    val state1 = block1.getState.asInstanceOf[Chest]
    val stack1 = state1.getInventory.getItem(slot1)
    var stack2 = new ItemStack(Material.AIR)
    if (table.get("item2")!=null && table.get("item2")!=LuaValue.NIL){
      val item2 = table.getTable("item2")
      val vector2 = item2.getVector("pos")
      val slot2 = item2.getInt("slot")
      val block2 = MainClass.world.getBlockAt(vector2.toLocation(MainClass.world))
      val state2 = block2.getState.asInstanceOf[Chest]
      stack2 = state2.getInventory.getItem(slot2)
    }
    val item3 = table.getTable("item3")
    val vector3 = item3.getVector("pos")
    val slot3 = item3.getInt("slot")
    val block3 = MainClass.world.getBlockAt(vector3.toLocation(MainClass.world))
    val state3 = block3.getState.asInstanceOf[Chest]
    val stack3 = state3.getInventory.getItem(slot3)
    return (stack1,stack2,stack3)
  }
  
  def this(l_table : LuaTable){
    this()
    val table = new LTable(l_table)
    table.foreachInt{x => 
       trades = trades :+ newTrade(new LTable(x._2.asInstanceOf[LuaTable]))
    }
  }
  private implicit def toNMS(stack : ItemStack) : net.minecraft.server.v1_8_R2.ItemStack = {
    return CraftItemStack.asNMSCopy(stack)
  }
}

class NPC(val position : Vector , val name : String , val trades : Trades, prof : Profession){
  private implicit def toNMS(stack : ItemStack) : net.minecraft.server.v1_8_R2.ItemStack = {
    return CraftItemStack.asNMSCopy(stack)
  }
  final def Spawn(world : World){
    var holder = new MetaHolder()
    holder.putBoolean("merchant", true)
    holder.put("name",name)
    holder.put("trades",trades)
    var spawned = world.spawn(position.toLocation(world), classOf[Villager])
    holder.attachTo(spawned)
    spawned.setAdult()
    spawned.setProfession(prof)
    spawned.setCustomNameVisible(false)
    var nms = spawned.asInstanceOf[CraftVillager].getHandle
    var comp = nms.getNBTTag
    if (comp==null){
      comp = new NBTTagCompound();
    }
    nms.c(comp)
    val br = nms.getClass.getDeclaredField("br")
    br.setAccessible(true)
    var offers = new MerchantRecipeList()
    trades.offers.foreach{trade=>
      val offer = new MerchantRecipe(trade._1,trade._2,trade._3)
      offers.add(offer)
    }
    br.set(nms, offers)
    comp.setInt("NoAI", 1)
    nms.f(comp)
    TextCreator.createTrackedText(name.replace("<p>",ChatColor.LIGHT_PURPLE + "").replace("<g>",ChatColor.GREEN+""), spawned, new Vector(0,0.125,0))
    Quests.kill = Quests.kill :+ spawned
  }
}