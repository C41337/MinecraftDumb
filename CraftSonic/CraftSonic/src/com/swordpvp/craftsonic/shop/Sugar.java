package com.swordpvp.craftsonic.shop;

import com.craftthatblock.ctbapi.ItemBuilder;
import com.swordpvp.craftsonic.API;
import com.swordpvp.craftsonic.ShopManager;
import com.swordpvp.swordlib.SwordLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Sugar implements ShopManager.ShopItem {


	@Override
	public ItemStack getItem(Player player) {
		return new ItemBuilder(Material.SUGAR).withName(ChatColor.WHITE + "Speed Boost").withLore(ChatColor.BLUE + "Click to have a speed boost!").withLore("").withLore(ChatColor.GOLD + "Price: 75 SwordPoints").toItemStack();
	}

	@Override
	public void open(Player player) {
		if (SwordLib.getSwordPlayer(player).getPoints() - 75 < 0) {
			API.say(player.getPlayer(), "You are missing " + (SwordLib.getSwordPlayer(player).getPoints() - 75) + " points!");
			return;
		}
		SwordLib.getSwordPlayer(player).setPoints(SwordLib.getSwordPlayer(player).getPoints() - 75);
		int sugar = 1;
		if (API.getSugar().containsKey(player.getName())) {
			sugar += API.getSugar().get(player.getName());
			API.getSugar().remove(player.getName());
			API.getSugar().put(player.getName(), sugar);
		} else {
			API.getSugar().put(player.getName(), sugar);
		}
		API.say(player.getPlayer(), "You have bought 1 speed boost! Total: " + ChatColor.YELLOW + sugar);
	}
}

