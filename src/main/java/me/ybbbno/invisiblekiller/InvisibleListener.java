package me.ybbbno.invisiblekiller;

import me.ybbbno.nvanish.PriorityManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class InvisibleListener implements Listener {
    private final InvisibleKiller plugin;
    private final PriorityManager manager;
    private final Map<String, String> temp = new HashMap<>();
    private final Set<String> isOverwriting = new HashSet<>();

    public InvisibleListener(InvisibleKiller plugin, PriorityManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (manager == null || !manager.isInit()) return;

        if (event.getEntity().getType() != EntityType.PLAYER) return;

        Player p = (Player) event.getEntity();
        PotionEffect npe = event.getNewEffect();
        PotionEffect ope = event.getOldEffect();

        if (ope == null && npe != null && npe.getType() == PotionEffectType.INVISIBILITY) {
            if (manager.isPlayerTabHidden(p)) {
                isOverwriting.add(p.getName());
            } else {
                manager.toggleTabHider(p);
            }
        }

        if (npe == null && ope != null && ope.getType() == PotionEffectType.INVISIBILITY) {
            if (!isOverwriting.contains(p.getName())) {
                manager.toggleTabHider(p);
            } else {
                isOverwriting.remove(p.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Player killer = player.getKiller();
        if (killer == null || !killer.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        Component playerName = Component.text(player.getName());
        Component unknownKiller = Component.translatable(TranslationKeys.UNKNOWN_KILLER, "Unknown");
        Component deathMsg = Component.translatable(TranslationKeys.DEATH, "%s was slain by %s").arguments(playerName, unknownKiller);

        event.deathMessage(deathMsg);

        plugin.logger.info(player.getName() + " was killed by " + killer.getName());

        if (plugin.isPlayerHeadsVanillaTweaks) {
            temp.put(player.getName(), killer.getName());
        }
    }

    // Vanilla Tweaks - Player Head Drops (v1.1.11 - v1.1.15)
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!plugin.isPlayerHeadsVanillaTweaks) return;

        Item item = event.getEntity();
        ItemStack stack = item.getItemStack();

        if (stack.getType() != Material.PLAYER_HEAD) return;

        List<Component> lore = stack.lore();
        if (lore == null || lore.isEmpty()) return;

        String pname = item.getName();
        if (!pname.contains("'s Head")) return;
        pname = pname.replace("'s Head", "");

        Component killer;

        try {
            killer = lore.getFirst().children().getFirst();
        } catch (NoSuchElementException exception) {
            return;
        }

        if (killer == null) return;

        String kname = temp.get(pname);
        if (kname != null && killer.toString().contains(kname)) {
            killer = Component.translatable(TranslationKeys.UNKNOWN_KILLER, "Unknown").color(NamedTextColor.YELLOW);
            temp.remove(pname);
        }

        Component desc = Component.translatable(TranslationKeys.KILLED_BY, "Killed by %s").arguments(killer).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        stack.lore(List.of(desc));

        item.setItemStack(stack);
    }
}
