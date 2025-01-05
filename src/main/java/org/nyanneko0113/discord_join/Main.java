package org.nyanneko0113.discord_join;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.nyanneko0113.discord_join.commands.DiscordCommand;
import org.nyanneko0113.discord_join.events.PlayerJoinListener;
import org.nyanneko0113.discord_join.manager.WhiteListManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Main extends JavaPlugin implements CommandExecutor, TabCompleter {

    public static final String TEXT_INFO = ChatColor.AQUA + "[DiscordJoin] " + ChatColor.RESET;
    public static final String TEXT_ERROR = ChatColor.RED + "[エラー] " + ChatColor.RESET;

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        registerCommands();
        saveDefaultConfig();

        try {
            DiscordUtil.startBot();
        } catch (LoginException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("プラグインが起動しました");
    }

    private void registerCommands() {
        getCommand("discord_whitelist").setExecutor(new DiscordCommand());
    }

    @Override
    public void onDisable() {
        DiscordUtil.stopBot();
    }

    public static Main getInstance() {
        return getPlugin(Main.class);
    }


}
