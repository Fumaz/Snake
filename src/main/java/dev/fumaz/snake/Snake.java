package dev.fumaz.snake;

import dev.fumaz.snake.command.SnakeCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Snake extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("snake").setExecutor(new SnakeCommand(this));
    }

}
