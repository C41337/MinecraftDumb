package com.swordpvp.minigames.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.minigames.API;
import com.swordpvp.minigames.MiniGame;
import com.swordpvp.minigames.MiniGamePlayer;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Disintergration implements MiniGame {
	/*

	spawn = spawn location

	 */
	int y;
	boolean breakblocks;
	int time;
	int timeout;
	Location spawn;
	Location spectate;
	List<MiniGamePlayer> winners = new ArrayList<MiniGamePlayer>();

	int size = 10;

	String world;

	@Override
	public String getName() {
		return "Disintergration";
	}

	@Override
	public void setup() {
		FileConfiguration config = API.getGamemodeConfig(getName());
		time = 5 * 10;
		breakblocks = false;
		winners.clear();


		String spawnString = config.getString("spawn");
		world = spawnString.split(":")[0];
		Bukkit.createWorld(new WorldCreator(world));
		spawn = CTBAPI.getLocationFromString(spawnString);

		y = spawn.getBlockY() - 3;
		spectate = spawn.clone();
		spectate.add(0, 10, 0);
		timeout = (int) (2.5 * 60);
		setupArena();

		// Move all players and add to alive
		for (MiniGamePlayer player : API.getMiniGamePlayers()) {
			// move
			API.resetPlayer(player);
			player.getPlayer().teleport(getSpawn());
		}

		API.broadcast(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Now Playing: " + ChatColor.RESET + getName());
		API.broadcast("Don't fall off! Blocks will slowly break around you!");
	}

	@Override
	public String getWorld() {
		return world;
	}

	@Override
	public void finish() {
		// Remove arena
		for (int x = -size; x < size; x++) {
			for (int z = -size; z < size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY(), spawn.getBlockZ() + z).getBlock();
				block.setType(Material.AIR);
			}
		}

	}

	@Override
	public void onGameTick() {
		if (time >= 0) {
			time--;
		}
		if (time == 0) {
			breakblocks = true;
		}
		if (time > 0) {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.setMessage(p.getPlayer(), ChatColor.YELLOW + "Time", ((float) time / (float) (5 * 10)) * 100);
				//setupArena();
			}
		} else {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.removeBar(p.getPlayer());
			}

			// age 3 blocks per tick
			for (int x = 0; x < 3; x++) {
				Random r = new Random();
				age(new Location(spawn.getWorld(), r.nextInt(size * 2) - size + spawn.getBlockX(), spawn.getBlockY(), r.nextInt(size * 2) - size + spawn.getBlockZ()).getBlock());
			}
		}

	}

	@Override
	public void onSecond() {
		//API.broadcast(time + " : " + breakblocks);

		update();
		if (timeout-- <= 0) {
			win();
		}

		/*for (int x = -size; x <= size; x++) {
		    for (int z = -size; z <= size; z++) {
				if (r.nextInt(100) < 20) {
					age(new Location(spawn.getWorld(), x + spawn.getBlockX(), spawn.getBlockY(), z + spawn.getBlockZ()).getBlock());
				}
			}
		}*/
	}

	public void win() {
		if (API.getMiniGamePlayers().size() - API.getSpectators().size() >= 1) {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				if (!API.isSpectator(p))
					winners.add(p);
			}
		}
		MiniGamePlayer player1 = null;
		MiniGamePlayer player2 = null;
		MiniGamePlayer player3 = null;
		if (winners.size() == 0) {

		} else if (winners.size() == 1) {
			player1 = winners.get(0);
		} else if (winners.size() == 2) {
			player1 = winners.get(1);
			player2 = winners.get(0);
		} else if (winners.size() >= 3) {
			player1 = winners.get(winners.size() - 1);
			player2 = winners.get(winners.size() - 2);
			player3 = winners.get(winners.size() - 3);
		}
		API.win(this, player1, player2, player3);
	}

	@Override
	public void spectate(MiniGamePlayer player) {
		player.getPlayer().teleport(spectate);
	}

	@Override
	public void update() {
		//API.broadcast(API.getMiniGamePlayers().size() + " - " + API.getSpectators().size());
		if (API.getMiniGamePlayers().size() - API.getSpectators().size() <= 1 && API.getMiniGamePlayers().size() > 1) {
			win();
		}

		API.updateInvisible();
	}

	@EventHandler
	public void onPlayerMoveEvent(final PlayerMoveEvent event) {
		// move, wait 4 tick, break block
		// if y is < 0, remove from alive players, make spectate

		if (event.getTo().getY() <= y) {
			// dead
			if (API.isSpectator(API.getMiniGamePlayer(event.getPlayer()))) {
				event.getPlayer().teleport(spectate);
			} else {
				MiniGamePlayer player = API.getMiniGamePlayer(event.getPlayer());
				winners.add(player);
				API.broadcast(player.getName() + " &7fell off!");
				API.spectate(player);
				update();
			}
		}
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		if (!API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (!API.isSpectator(API.getMiniGamePlayer(event.getPlayer())))
			event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player)
			event.setCancelled(true);
	}

	public void age(Block b) {
		if (b.getType() == Material.WOOL) {
			Random r = new Random();
			BlockState bs = b.getState();
			Wool wool = (Wool) bs.getData();

			if (wool.getColor() == DyeColor.WHITE) {
				b.setData((byte) 4);
			} else if (wool.getColor() == DyeColor.YELLOW) {
				b.setData((byte) 1);
			} else if (wool.getColor() == DyeColor.ORANGE) {
				b.setData((byte) 14);
			} else if (wool.getColor() == DyeColor.RED) {
				b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
				b.setType(Material.AIR);
			}
			//b.setType(Material.DIAMOND_BLOCK);
			//b.getState().update(true);
			//API.broadcast(b.toString());
		}
	}

	public void setupArena() {
		// Make arena

		for (int x = -size; x < size; x++) {
			for (int z = -size; z < size; z++) {
				Block block = new Location(spawn.getWorld(), spawn.getBlockX() + x, spawn.getBlockY(), spawn.getBlockZ() + z).getBlock();
				block.setType(Material.WOOL);
				block.setData((byte) 0);
			}
		}
	}

	public Location getSpawn() {
		Location spawn = this.spawn.clone();
		spawn.add(0, 2, 0);
		return spawn;
	}
}


