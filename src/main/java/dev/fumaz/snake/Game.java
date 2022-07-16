package dev.fumaz.snake;

import dev.fumaz.commons.bukkit.misc.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game implements Listener {

    private static final int WIDTH = 40;
    private static final int HEIGHT = 40;

    private static final World world = Bukkit.getWorld("world");
    private static final Location spawn = new Location(world, 119.5, 128.5, 240.5, 90, 60);
    private static final Location min = new Location(world, 115, 106, 260);
    private static final Location max = new Location(world, 76, 106, 221);

    private static final BoundingBox box = BoundingBox.of(min, max);

    private final Player player;

    private int length = 1;
    private int appleX = 10;
    private int appleY = 10;

    private Direction direction = Direction.RIGHT;

    private final List<Integer[]> tail = new ArrayList<>();

    private final BukkitTask moveTask;

    public Game(JavaPlugin plugin, Player player) {
        this.player = player;

        prepareArena();

        player.teleport(spawn);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        tail.add(new Integer[]{0, 0});
        Location appleLocation = new Location(world, min.getX() - appleX, min.getY(), min.getZ() - appleY);
        appleLocation.getBlock().setType(Material.RED_WOOL);

        moveTask = Scheduler.of(plugin).runTaskTimer(this::move, 0, 5);
    }

    public void move() {
        Integer[] head = tail.get(0);
        Integer[] newHead = new Integer[]{head[0] + direction.y, head[1] + direction.x};
        tail.add(0, newHead);

        Integer[] removed = tail.remove(tail.size() - 1);

        if (newHead[0] == appleX && newHead[1] == appleY) {
            appleX = (int) (Math.random() * WIDTH);
            appleY = (int) (Math.random() * HEIGHT);

            Location appleLocation = new Location(world, min.getX() - appleX, min.getY(), min.getZ() - appleY);
            appleLocation.getBlock().setType(Material.RED_WOOL);

            length++;

            tail.add(new Integer[]{appleX + direction.y, appleY + direction.x});
        }

        Location removedLocation = new Location(world, min.getX() - removed[0], min.getY(), min.getZ() - removed[1]);
        Location headLocation = new Location(world, min.getX() - newHead[0], min.getY(), min.getZ() - newHead[1]);

        if (headLocation.getX() < box.getMinX() || headLocation.getX() > box.getMaxX() || headLocation.getZ() < box.getMinZ() || headLocation.getZ() > box.getMaxZ()) {
            player.sendMessage("You lost!");
            moveTask.cancel();
            HandlerList.unregisterAll(this);
            return;
        }

        long amount = tail.stream()
                .filter(t -> Objects.equals(t[0], newHead[0]) && Objects.equals(t[1], newHead[1]))
                .count();

        if (amount > 1) {
            player.sendMessage("You lost!");
            moveTask.cancel();
            HandlerList.unregisterAll(this);
            return;
        }

        removedLocation.getBlock().setType(Material.GRASS_BLOCK);
        headLocation.getBlock().setType(Material.LIME_WOOL);
    }

    private void prepareArena() {
        for (int x = (int) box.getMinX(); x <= box.getMaxX(); x++) {
            for (int y = (int) box.getMinY(); y <= box.getMaxY(); y++) {
                for (int z = (int) box.getMinZ(); z <= box.getMaxZ(); z++) {
                    Location location = new Location(world, x, y, z);

                    location.getBlock().setType(Material.GRASS_BLOCK);
                }
            }
        }
    }

    @EventHandler
    public void onVehicleMove(PlayerMoveEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        Vector subtracted = to.toVector().subtract(from.toVector());

        if (Math.max(Math.abs(subtracted.getX()), Math.abs(subtracted.getZ())) == Math.abs(subtracted.getX())) {
            if (subtracted.getX() > 0) {
                direction = Direction.DOWN;
            } else {
                direction = Direction.UP;
            }
        } else {
            if (subtracted.getZ() > 0) {
                direction = Direction.LEFT;
            } else {
                direction = Direction.RIGHT;
            }
        }

        event.setCancelled(true);
    }

    private enum Direction {
        LEFT(-1, 0),
        RIGHT(1, 0),
        UP(0, 1),
        DOWN(0, -1);

        private final int x;
        private final int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
