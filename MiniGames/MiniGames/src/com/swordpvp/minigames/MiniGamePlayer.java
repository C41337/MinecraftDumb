package com.swordpvp.minigames;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MiniGamePlayer {

	@Getter
	private final String name;

	public MiniGamePlayer(String name) {
		this.name = name;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(getName());
	}

}
