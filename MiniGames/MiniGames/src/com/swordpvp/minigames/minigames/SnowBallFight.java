package com.swordpvp.minigames.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.minigames.API;
import com.swordpvp.minigames.MiniGame;
import com.swordpvp.minigames.MiniGamePlayer;
import com.swordpvp.swordlib.SwordLib;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SnowBallFight implements MiniGame {
	/*

	spawn = spawn location

	 */
	boolean breakblocks;
	int time;
	Location spawn;
	Location spectate;
	int timeout;
	String world;
	List<MiniGamePlayer> winners = new ArrayList<MiniGamePlayer>();


	@Override
	public String getName() {
		return "SnowBallFight";
	}

	@Override
	public void setup() {

		API.clearSpectator();
		FileConfiguration config = API.getGamemodeConfig(getName());

		time = 5 * 10;
		breakblocks = false;
		winners.clear();
		String spawnString = config.getString("spawn");
		world = spawnString.split(":")[0];
		Bukkit.createWorld(new WorldCreator(world));
		spawn = CTBAPI.getLocationFromString(spawnString);
		spectate = spawn.clone();
		spectate.add(0, 10, 0);

		timeout = 2 * 60;

		// Move all players and add to alive
		for (MiniGamePlayer player : API.getMiniGamePlayers()) {
			// move
			API.resetPlayer(player);
			player.getPlayer().teleport(getSpawn());

		}
		API.broadcast(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Now Playing: " + ChatColor.RESET + "SnowBall Fight");
		API.broadcast("Break snow to get snowballs and throw them at players to kill them!");

	}

	@Override
	public void onGameTick() {
		if (time >= 0) {
			time--;
		}
		if (time == 0) {
			breakblocks = true;
			ItemStack item = new ItemStack(Material.STONE_SPADE);
			item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			ItemStack item_ = item.clone();
			item_.setType(Material.IRON_SPADE);
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.removeBar(p.getPlayer());
				if (!API.isSpectator(p)) {

					if (SwordLib.getSwordPlayer(p.getName()).getTopDonatorRank() != "")

						p.getPlayer().getInventory().addItem(item_);
					else
						p.getPlayer().getInventory().addItem(item);

				}
			}
		}
		if (time > 0) {
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.setMessage(p.getPlayer(), ChatColor.YELLOW + "Time", ((float) time / (float) (5 * 10)) * 100);


				//setupArena();
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

	@Override
	public String getWorld() {
		return world;
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		event.setCancelled(true);
		if (breakblocks && !API.isSpectator(API.getMiniGamePlayer(event.getPlayer())) && event.getBlock() != null && (event.getBlock().getType() == Material.SNOW || event.getBlock().getType() == Material.SNOW_BLOCK)) {
			event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 1));
		}
	}


	@EventHandler
	public void onCraft(final CraftItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onPickup(final PlayerPickupItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(final PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			event.setCancelled(true);
			if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Snowball && breakblocks) {

				MiniGamePlayer player = API.getMiniGamePlayer((Player) event.getEntity());
				winners.add(player);
				API.broadcast(player.getName() + " &7got hit my a snowball!");

				API.spectate(player);
				update();

			}
		}
	}

	@Override
	public void finish() {

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

	public Location getSpawn() {
		return spawn;
	}
}


