package com.guyde.plug.data;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.projectiles.ProjectileSource;

public class JavaHelper {
	public static ProjectileSource getShooter(Arrow arrow){
		return arrow.getShooter();
	}
	
	public static double getMaxHealth(LivingEntity ent){
		return ent.getMaxHealth();
	}
}
