package com.swordpvp.minigames;

import org.bukkit.event.Listener;

public interface MiniGame extends Listener {

	public String getName();

	public void setup();

	public void finish();

	public void onGameTick();

	public void onSecond();

	public void spectate(MiniGamePlayer player);

	public void update();

	public String getWorld();

}
