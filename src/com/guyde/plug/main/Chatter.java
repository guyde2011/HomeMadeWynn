package com.guyde.plug.main;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class Chatter{
	
	final String name;
	
	public Chatter(String str){
		name = str;
	}
	
	public void writeMessage(Player player, String message){
		player.sendMessage(name + message);
	}
	
	public void writeMessage(List<Player> players, String message){
		for (Player player : players){
			writeMessage(player,message);
		}
	}
	
	public void broadcast(String message){
		Bukkit.broadcastMessage(name + message);
	}
	
	public void informConsole(String message){
		Bukkit.getConsoleSender().sendMessage(name + message);
	}
	
	
}
