package com.swordpvp.craftsonic;

import com.craftthatblock.ctbapi.CTBAPI;
import com.craftthatblock.ctbapi.ItemBuilder;
import com.swordpvp.swordlib.SwordLib;
import com.swordpvp.swordlib.SwordPlayer;
import lombok.Getter;
import lombok.Setter;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffectType;

public class Sonic {
	@Getter
	final private String name;
	@Setter
	@Getter
	private int checkpoint, timeout, sugar;


	/**
	 * Make a new Sonic
	 *
	 * @param name Player name of Hades
	 */
	public Sonic(String name) {
		this.name = name;
		checkpoint = 0;
		timeout = 90;
		if (API.getSugar().containsKey(name))
			sugar = API.getSugar().get(name);
		else sugar = 0;
	}

	/**
	 * Get Player instance of Sonic
	 *
	 * @return player
	 */
	public Player getPlayer() {
		return Bukkit.getPlayer(name);
	}

	public SwordPlayer getSwordPlayer() {
		return SwordLib.getSwordPlayer(getPlayer());
	}


	// Spawn a player
	public void spawn() {
		CTBAPI.resetPlayer(getPlayer());
		getPlayer().teleport(API.getInstance().getSpawn());

		getPlayer().getInventory().setHelmet(getArmor(Material.LEATHER_HELMET, Color.BLUE));
		getPlayer().getInventory().setChestplate(getArmor(Material.LEATHER_CHESTPLATE, Color.BLUE));
		getPlayer().getInventory().setLeggings(getArmor(Material.LEATHER_LEGGINGS, Color.BLUE));
		getPlayer().getInventory().setBoots(getArmor(Material.LEATHER_BOOTS, Color.RED));

		getPlayer().addPotionEffect(CTBAPI.getInfinitePotionFromString("SPEED 2"));
		getPlayer().addPotionEffect(CTBAPI.getInfinitePotionFromString("JUMP 2"));

		getPlayer().addPotionEffect(CTBAPI.getInfinitePotionFromString(PotionEffectType.NIGHT_VISION.getName() + " 0"));

		getPlayer().getInventory().addItem(new ItemBuilder(Material.COMPASS).withName(ChatColor.WHITE + "Checkpoint Finder").withLore(ChatColor.BLUE + "Points to the next checkpoint").toItemStack());
		getPlayer().getInventory().addItem(new ItemBuilder(Material.NETHER_STAR).withName(ChatColor.WHITE + "Respawner").withLore(ChatColor.BLUE + "Click to respawn to the latest checkpoint").toItemStack());
		if (sugar >= 1) {
			getPlayer().getInventory().addItem(new ItemBuilder(Material.SUGAR).withName(ChatColor.WHITE + "Speed Boost").withLore(ChatColor.BLUE + "Click to have a speed boost!").withAmount(sugar).toItemStack());

		}
		getPlayer().setWalkSpeed(0.2f);


		BarAPI.setMessage(getPlayer(), ChatColor.YELLOW + "In-Game", ((float) API.getInstance().matchtime / (float) API.getInstance().maxtime) * 100);
	}


	public void done() {

		API.broadcast(getPlayer().getName() + " is done!");
		API.say(getPlayer(), "You finished in " + (API.getInstance().maxtime - API.getInstance().matchtime) + " seconds!");
		API.getInstance().moveToLobby(getPlayer());
		API.removeSonic(this);
		API.getInstance().winners.add(new CraftSonic.TimeHolder(API.getInstance().maxtime - API.getInstance().matchtime, getName()));


		API.getInstance().updateScoreboard();

	}

	private ItemStack getArmor(Material material, Color color) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(color);
		item.setItemMeta(meta);
		//item.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
		return item;
	}

}
