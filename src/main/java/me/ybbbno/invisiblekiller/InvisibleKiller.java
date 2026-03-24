package me.ybbbno.invisiblekiller;

import io.papermc.paper.datapack.DiscoveredDatapack;
import me.deadybbb.ybmj.PluginProvider;
import me.ybbbno.nvanish.NVanishAPI;
import me.ybbbno.nvanish.PriorityManager;

public final class InvisibleKiller extends PluginProvider {
    public boolean isPlayerHeadsVanillaTweaks;

    @Override
    public void onEnable() {
        PriorityManager manager = null;

        try {
            manager = NVanishAPI.getAPI().getManager();
        } catch (Exception ex) {
            logger.warning("NVanish is not found!");
        }

        getServer().getPluginManager().registerEvents(new InvisibleListener(this, manager), this);

        isPlayerHeadsVanillaTweaks = getServer().getDatapackManager().getEnabledPacks()
                .stream().map(DiscoveredDatapack::getDescription)
                .anyMatch(c -> c.toString().contains("Player Head Drops"));
        if (isPlayerHeadsVanillaTweaks) {
            logger.info("Vanilla Tweaks - Player Head Drops datapack was found!");
        }
    }

    @Override
    public void onDisable() {
        // pass
    }
}
