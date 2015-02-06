package com.swordpvp.minigames;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TimeM implements Listener {
	public TimeM() {
		API.getInstance().getServer().getPluginManager().registerEvents(this, API.getInstance());
	}

	@EventHandler
	public void onSecond(final SecondEvent event) {

	}
}
