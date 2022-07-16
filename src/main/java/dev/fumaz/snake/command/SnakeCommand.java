package dev.fumaz.snake.command;

import dev.fumaz.commons.bukkit.command.PlayerCommandExecutor;
import dev.fumaz.snake.Game;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SnakeCommand implements PlayerCommandExecutor {

    private final JavaPlugin plugin;
    private Game game;

    public SnakeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player player, @NotNull Command command, @NotNull String[] strings) {
        if (game != null) {
            player.sendMessage(ChatColor.RED + "You are already in a game!");
            return;
        }

        game = new Game(plugin, player);
    }

}
