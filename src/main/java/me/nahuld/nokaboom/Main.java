package me.nahuld.nokaboom;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT)
            return;

        Set<Block> blown = Sets.newHashSet(event.blockList());
        event.blockList().clear(); // clean it now, breaks will be blown if not

        bukkitRunnable(() -> { // async
            Set<Block> blocks = Sets.newHashSet();
            blocks.addAll(
                    blown.stream()
                            .filter(this::hasEmeraldNear)
                            .collect(Collectors.toSet()));

            bukkitRunnable(() -> { // sync
                blocks.forEach(Block::breakNaturally);
            }).runTask(this);
        }).runTaskAsynchronously(this);
    }

    private boolean hasEmeraldNear(Block block) {
        boolean emerald = false;
        for (int y = 0; y <= 255; y++) {
            if (block.getWorld().getBlockAt(block.getX(), y, block.getZ()).getType() != Material.EMERALD_BLOCK)
                continue;
            emerald = true;
            break;
        }
        return !emerald; // we want to return those who don't have an emerald "near" em
    }

    private static BukkitRunnable bukkitRunnable(final Runnable r) {
        return new BukkitRunnable(){
            @Override
            public void run(){
                r.run();
            }
        };
    }
}
