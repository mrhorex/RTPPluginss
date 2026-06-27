package ru.yourplugin.rtp;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class RTPPlugin extends JavaPlugin implements CommandExecutor {

    private Random random = new Random();
    private int radius = 5000;

    @Override
    public void onEnable() {
        getCommand("rtp").setExecutor(this);
        getCommand("randomtp").setExecutor(this);
        getLogger().info("§aRTP Plugin включен!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage("§cРТП работает только в обычном мире!");
            return true;
        }

        if (player.hasCooldown(Material.DIAMOND)) {
            long secondsLeft = player.getCooldown(Material.DIAMOND) / 20;
            if (secondsLeft > 0) {
                player.sendMessage("§cПодожди ещё §e" + secondsLeft + " §cсекунд до следующего РТП!");
            } else {
                player.sendMessage("§cПодожди немного перед следующим РТП!");
            }
            return true;
        }

        player.sendMessage("§aПоиск безопасного места...");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        spawnParticles(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLocation = findSafeLocation(player.getWorld());

                if (safeLocation == null) {
                    player.sendMessage("§cНе удалось найти безопасное место! Попробуй снова.");
                    return;
                }

                player.teleport(safeLocation);

                spawnParticles(player);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

                player.sendMessage("§a=================================");
                player.sendMessage("§a✓ Телепорт выполнен успешно!");
                player.sendMessage("§e📍 Координаты: §fX: " + safeLocation.getBlockX() + 
                                  " §fY: " + safeLocation.getBlockY() + 
                                  " §fZ: " + safeLocation.getBlockZ());
                player.sendMessage("§a=================================");

                player.setCooldown(Material.DIAMOND, 25 * 20);

                player.sendTitle("§aТелепорт завершён!",
                        "§eX: " + safeLocation.getBlockX() + " §aY: " + safeLocation.getBlockY() + " §aZ: " + safeLocation.getBlockZ(),
                        10, 60, 20);
            }
        }.runTaskLater(this, 20);
        return true;
    }

    private Location findSafeLocation(World world) {
        int spawnX = 0, spawnZ = 0;
        for (int attempt = 0; attempt < 30; attempt++) {
            int angle = random.nextInt(360);
            int distance = random.nextInt(radius) + 100;

            int x = spawnX + (int) (distance * Math.cos(Math.toRadians(angle)));
            int z = spawnZ + (int) (distance * Math.sin(Math.toRadians(angle)));

            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y, z + 0.5);

            if (isSafeLocation(loc)) {
                return loc;
            }
        }
        return null;
    }

    private boolean isSafeLocation(Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        Material feet = world.getBlockAt(x, y, z).getType();
        Material below = world.getBlockAt(x, y - 1, z).getType();
        Material head = world.getBlockAt(x, y + 1, z).getType();

        return below.isSolid() &&
                !feet.isSolid() &&
                !head.isSolid() &&
                feet != Material.LAVA &&
                feet != Material.WATER &&
                below != Material.LAVA &&
                below != Material.WATER;
    }

    private void spawnParticles(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 1.5 + random.nextDouble() * 2;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;
            double yOffset = random.nextDouble() * 2;

            world.spawnParticle(Particle.END_ROD,
                    loc.getX() + xOffset,
                    loc.getY() + 0.5 + yOffset,
                    loc.getZ() + zOffset,
                    1, 0, 0, 0, 0);
        }

        for (int i = 0; i < 20; i++) {
            double angle = i * 0.5;
            double radius = 2;
            world.spawnParticle(Particle.SPELL_WITCH,
                    loc.getX() + Math.cos(angle) * radius,
                    loc.getY() + 0.5 + i * 0.1,
                    loc.getZ() + Math.sin(angle) * radius,
                    1, 0, 0, 0, 0);
        }
    }
}
