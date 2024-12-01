package wgextender.features.claimcommand;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.commands.task.RegionAdder;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.DomainInputResolver.UserLocatorPolicy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wgextender.Config;
import wgextender.WGExtender;
import wgextender.utils.WEUtils;
import wgextender.utils.WGRegionUtils;

public class WGClaimCommand {

	protected static void claim(String id, CommandSender sender) throws CommandException {
		Config config = WGExtender.getInstance().getLocalConfig();
		if (!(sender instanceof Player player)) {
			throw new CommandException(config.getMessage("region-claimed.error.player"));
		}
		if (id.equalsIgnoreCase("__global__")) {
			throw new CommandException(config.getMessage("region-claimed.error.global"));
		}
		if (!ProtectedRegion.isValidId(id) || id.startsWith("-")) {
			throw new CommandException(config.getMessage("region-claimed.error.name") + id + config.getMessage("region-claimed.error.forbidden"));
		}

        BukkitWorldConfiguration wcfg = WGRegionUtils.getWorldConfig(player);

		if (wcfg.maxClaimVolume == Integer.MAX_VALUE) {
			throw new CommandException(config.getMessage("region-claimed.error.volume") + config.getMessage("region-claimed.error.volume-current") + Integer.MAX_VALUE + config.getMessage("region-claimed.error.volume-hint"));
		}

		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
		RegionPermissionModel permModel = new RegionPermissionModel(localPlayer);

		if (!permModel.mayClaim()) {
			throw new CommandPermissionsException();
		}

		RegionManager manager = WGRegionUtils.getRegionManager(player.getWorld());

		if (manager.hasRegion(id)) {
			throw new CommandException(config.getMessage("region-claimed.error.name-exists"));
		}

		ProtectedRegion region = createProtectedRegionFromSelection(player, id);

		if (!permModel.mayClaimRegionsUnbounded()) {
			int maxRegionCount = wcfg.getMaxRegionCount(localPlayer);
			if ((maxRegionCount >= 0) && (manager.getRegionCountOfPlayer(localPlayer) >= maxRegionCount)) {
				throw new CommandException(config.getMessage("region-claimed.error.count"));
			}
			if (region.volume() > wcfg.maxClaimVolume) {
				throw new CommandException(config.getMessage("region-claimed.error.size") + wcfg.maxClaimVolume + config.getMessage("region-claimed.error.size-hint") + region.volume());
			}
		}

		ApplicableRegionSet regions = manager.getApplicableRegions(region);

		if (regions.size() > 0) {
			if (!regions.isOwnerOfAll(localPlayer)) {
				throw new CommandException(config.getMessage("region-claimed.error.overlap"));
			}
		} else if (wcfg.claimOnlyInsideExistingRegions) {
			throw new CommandException(config.getMessage("region-claimed.error.restricted"));
		}

		RegionAdder task = new RegionAdder(manager, region);
		task.setLocatorPolicy(UserLocatorPolicy.UUID_ONLY);
		task.setOwnersInput(new String[] { player.getName() });
		try {
			task.call();
			sender.sendMessage(ChatColor.YELLOW + config.getMessage("region-claimed.success") + id);
		} catch (Exception e) {
			sender.sendMessage(ChatColor.YELLOW + config.getMessage("region-claimed.error.unknown") + id);
			e.printStackTrace();
		}
	}

	private static ProtectedRegion createProtectedRegionFromSelection(Player player, String id) throws CommandException {
		Config config = WGExtender.getInstance().getLocalConfig();
		try {
			Region selection = WEUtils.getSelection(player);
			if (selection instanceof CuboidRegion) {
				return new ProtectedCuboidRegion(id, selection.getMinimumPoint(), selection.getMaximumPoint());
			} else {
				throw new CommandException(config.getMessage("region-selection.error.shape"));
			}
		} catch (IncompleteRegionException e) {
			throw new CommandException(config.getMessage("region-selection.error.select"));
		}

	}

}
