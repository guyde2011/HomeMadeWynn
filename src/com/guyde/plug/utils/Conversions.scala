package com.guyde.plug.utils

import com.guyde.plug.data.PlayerDataManager
import org.bukkit.entity.Player
import com.guyde.plug.data.GameClass

object Conversions{
  implicit def player_to_class(player : Player) : GameClass = {
    return PlayerDataManager.GetClass(player)
  }
}