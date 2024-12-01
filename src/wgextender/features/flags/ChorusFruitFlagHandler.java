package wgextender.features.flags;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import wgextender.Config;
import wgextender.WGExtender;
import wgextender.utils.WGRegionUtils;

public class ChorusFruitFlagHandler implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onItemUse(PlayerItemConsumeEvent event) {
		Config config = WGExtender.getInstance().getLocalConfig();
		if (event.getItem().getType() == Material.CHORUS_FRUIT) {
			Player player = event.getPlayer();
			if (
				!WGRegionUtils.canBypassProtection(event.getPlayer()) &&
				!WGRegionUtils.isFlagAllows(player, player.getLocation(), WGExtenderFlags.CHORUS_FRUIT_USE_FLAG)
			) {
				player.sendMessage(ChatColor.RED + config.getMessage("flags.error.chorus"));
				event.setCancelled(true);
			}
		}
	}

}
