package com.swordpvp.minigames;

import com.swordpvp.minigames.minigames.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniGames extends JavaPlugin {


	protected long time = 0;

	public void onEnable() {
		new API(this);
		new Events();
		new Commander();
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {

				API.getCurrentMiniGame().onGameTick();
				if (System.currentTimeMillis() >= time) {
					time = System.currentTimeMillis() + 1000;
					API.updateInvisible();
					for (World world : Bukkit.getWorlds()) {
						world.setStorm(false);
						world.setThundering(false);
					}
					API.getCurrentMiniGame().onSecond();
				}
			}
		}, 2L, 2L);

		try {
			API.registerMiniGame(new TNTRun());
			API.registerMiniGame(new Paintball());
			API.registerMiniGame(new ColorMatch());
			API.registerMiniGame(new Disintergration());
			API.registerMiniGame(new SnowBallFight());
			API.registerMiniGame(new Spleef());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		API.startRandomMiniGame();
	}

	public void onDisable() {
		API.getCurrentMiniGame().finish();
	}

}
