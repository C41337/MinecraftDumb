package com.swordpvp.minigames.minigames;

import com.craftthatblock.ctbapi.CTBAPI;
import com.swordpvp.minigames.API;
import com.swordpvp.minigames.MiniGame;
import com.swordpvp.minigames.MiniGamePlayer;
import com.swordpvp.swordlib.SwordLib;
import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

/*
 * @author Hexcept
 */

public class Paintball implements MiniGame {
	/*

	bluespawn
	redspawn
	spectate

	 */
	int time;
	Location redSpawn;
	Location blueSpawn;
	Location spectate;
	int timeout;
	String world;
	List<MiniGamePlayer> winners = new ArrayList<MiniGamePlayer>();
	List<MiniGamePlayer> red = new ArrayList<MiniGamePlayer>();
	List<MiniGamePlayer> blue = new ArrayList<MiniGamePlayer>();

	List<MiniGamePlayer> redPlayed = new ArrayList<MiniGamePlayer>();
	List<MiniGamePlayer> bluePlayed = new ArrayList<MiniGamePlayer>();


	@Override
	public String getName() {
		return "Paintball";
	}

	@Override
	public void setup() {

		API.clearSpectator();
		FileConfiguration config = API.getGamemodeConfig(getName());

		time = 5 * 10;
		winners.clear();
		red.clear();
		blue.clear();
		String spawnString = config.getString("spectate");
		world = spawnString.split(":")[0];
		Bukkit.createWorld(new WorldCreator(world));
		spectate = CTBAPI.getLocationFromString(spawnString);
		redSpawn = CTBAPI.getLocationFromString(config.getString("redspawn"));
		blueSpawn = CTBAPI.getLocationFromString(config.getString("bluespawn"));

		timeout = 3 * 60;

		// Move all players and add to alive
		for (MiniGamePlayer player : API.getMiniGamePlayers()) {
			// move
			API.resetPlayer(player);
			// if blue has more, add the red
			DyeColor dyeColor = null;
			Color color = null;
			if (blue.size() > red.size()) {
				// add to red
				red.add(player);
				redPlayed.add(player);
				player.getPlayer().teleport(redSpawn);


				dyeColor = DyeColor.RED;
				color = Color.RED;
			} else {
				// add to blue
				blue.add(player);
				bluePlayed.add(player);
				player.getPlayer().teleport(blueSpawn);
				dyeColor = DyeColor.BLUE;
				color = Color.BLUE;
			}

			ItemStack wool = new ItemStack(Material.WOOL, 1, dyeColor.getData());

			ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta clam = (LeatherArmorMeta) chestplate.getItemMeta();
			clam.setColor(color);
			chestplate.setItemMeta(clam);

			ItemStack leggings = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta llam = (LeatherArmorMeta) leggings.getItemMeta();
			llam.setColor(color);
			leggings.setItemMeta(llam);

			ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
			LeatherArmorMeta blam = (LeatherArmorMeta) boots.getItemMeta();
			blam.setColor(color);
			boots.setItemMeta(blam);

			player.getPlayer().getInventory().setHelmet(wool);
			player.getPlayer().getInventory().setChestplate(chestplate);
			player.getPlayer().getInventory().setLeggings(leggings);
			player.getPlayer().getInventory().setBoots(boots);

		}
		API.broadcast(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Now Playing: " + ChatColor.RESET + "Paintball");
		API.broadcast("Throw snowballs at the other team to color them! If you are fully colored, you died! Last team standing!");

	}

	@Override
	public void onGameTick() {
		if (time >= 0) {
			time--;
		}
		if (time == 0) {
			ItemStack item = new ItemStack(Material.SNOW_BALL, 64);
			for (MiniGamePlayer p : API.getMiniGamePlayers()) {
				BarAPI.removeBar(p.getPlayer());
				if (!API.isSpectator(p)) {
					PlayerInventory inventory = p.getPlayer().getInventory();
					for (int x = 0; x < 9; x++)
						inventory.setItem(x, item);
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
		update();

		if (timeout-- <= 0) {
			win();
		}
	}

	@Override
	public void spectate(MiniGamePlayer player) {
		player.getPlayer().teleport(spectate);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void leave(PlayerQuitEvent event) {
		red.remove(API.getMiniGamePlayer(event.getPlayer()));
		blue.remove(API.getMiniGamePlayer(event.getPlayer()));
	}

	@Override
	public void update() {
		//API.broadcast(API.getMiniGamePlayers().size() + " - " + API.getSpectators().size());
		if ((red.size() < 1 || blue.size() < 1) && Bukkit.getOnlinePlayers().length > 1) {
			win();
		}
		API.updateInvisible();

		// TODO: Scoreboard
	}

	@Override
	public String getWorld() {
		return world;
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {
		event.setCancelled(true);
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
			if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Snowball) {

				MiniGamePlayer player = API.getMiniGamePlayer((Player) event.getEntity());

				boolean damaged = false;
				boolean die = false;

				while (!damaged) {
					LeatherArmorMeta clam = (LeatherArmorMeta) player.getPlayer().getInventory().getChestplate().getItemMeta();
					LeatherArmorMeta llam = (LeatherArmorMeta) player.getPlayer().getInventory().getLeggings().getItemMeta();
					LeatherArmorMeta blam = (LeatherArmorMeta) player.getPlayer().getInventory().getBoots().getItemMeta();
					if (clam.getColor().equals(Color.fromRGB(150, 50, 200)) &&
							llam.getColor().equals(Color.fromRGB(150, 50, 200)) &&
							blam.getColor().equals(Color.fromRGB(150, 50, 200))) {
						damaged = true;
						die = true;
					} else {
						int Min = 1;
						int Max = 3;
						int random = Min + (int) (Math.random() * ((Max - Min) + 1));
						if (random == 1) {
							if (clam.getColor() != Color.fromRGB(150, 50, 200)) {
								clam.setColor(Color.fromRGB(150, 50, 200));
								player.getPlayer().getInventory().getChestplate().setItemMeta(clam);
								damaged = true;
							}
						} else if (random == 2) {
							if (llam.getColor() != Color.fromRGB(150, 50, 200)) {
								llam.setColor(Color.fromRGB(150, 50, 200));
								player.getPlayer().getInventory().getLeggings().setItemMeta(llam);
								damaged = true;
							}
						} else if (random == 3) {
							if (blam.getColor() != Color.fromRGB(150, 50, 200)) {
								blam.setColor(Color.fromRGB(150, 50, 200));
								player.getPlayer().getInventory().getBoots().setItemMeta(blam);
								damaged = true;
							}
						} else {
							damaged = true;
						}
					}
				}

				if (die) {
					if (red.contains(player)) {
						red.remove(player);
					} else if (blue.contains(player)) {
						blue.remove(player);
					}
					API.spectate(player);
					API.broadcast(player.getName() + " &7has died!");
				}

				update();
			}
		}
	}

	@Override
	public void finish() {

	}

	public void win() {
		if (red.size() < 1) {
			//blue win
			API.broadcast(ChatColor.AQUA + ChatColor.BOLD.toString() + "First Place: " + ChatColor.AQUA + "Blue!");
			API.broadcast(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Second Place: " + ChatColor.RED + "Red!");
			for (MiniGamePlayer p : bluePlayed) {
				if (p.getPlayer().isOnline())
					SwordLib.getSwordPlayer(p.getPlayer()).addPoints(8);
			}
		} else if (blue.size() < 1) {
			// red win
			API.broadcast(ChatColor.AQUA + ChatColor.BOLD.toString() + "First Place: " + ChatColor.RED + "Red!");
			API.broadcast(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Second Place: " + ChatColor.AQUA + "Blue!");
			for (MiniGamePlayer p : redPlayed) {
				if (p.getPlayer().isOnline())

					SwordLib.getSwordPlayer(p.getPlayer()).addPoints(8);
			}
		}


		API.stopMiniGame(this);
	}
}


