package com.swordpvp.aurora;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AuroraTeam {

	public String name;
	public String color;
	public List<Location> spawns = new ArrayList<Location>();
	public int points = 0;

	public AuroraTeam(String name, String color) {
		this.name = name;
		this.color = color;
	}

	public void addSpawn(Location loc) {
		spawns.add(loc);
	}

	public Location getRandomSpawn() {
		return spawns.get((new Random()).nextInt(spawns.size()));
	}

	public void addPoints(int point) {
		this.points += point;
	}

	public Color getColor() {
		return Aurora.getColorFromString(color);
	}

	public ChatColor getChatColor() {
		return Aurora.getChatColorFromString(color);
	}

}
