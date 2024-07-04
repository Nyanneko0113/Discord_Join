package org.nyanneko0113.discord_join;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.nyanneko0113.discord_join.events.PlayerJoinListener;
import org.nyanneko0113.discord_join.manager.WhiteListManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public final class Main extends JavaPlugin implements CommandExecutor {

    private static final String TEXT_INFO = ChatColor.AQUA + "[DiscordJoin] " + ChatColor.RESET;
    private static final String TEXT_ERROR = ChatColor.RED + "[エラー] " + ChatColor.RESET;

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
        getCommand("discord_whitelist").setExecutor(this);
    }

    @Override
    public void onDisable() {
        DiscordUtil.stopBot();
    }

    public static Main getInstance() {
        return getPlugin(Main.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender send, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args[0].equalsIgnoreCase("add")) {
            try {
                if (!WhiteListManager.getPlayers().contains(args[1])) {
                    WhiteListManager.addPlayer(args[1]);
                    send.sendMessage(Main.TEXT_INFO + args[1] + "をホワイトリストから追加しました");
                }
                else {
                    send.sendMessage(Main.TEXT_ERROR + "そのプレイヤーは既に登録されています。");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (args[0].equalsIgnoreCase("remove")) {
            try {
                if (WhiteListManager.getPlayers().contains(args[1])) {
                    WhiteListManager.removePlayer(args[1]);
                    send.sendMessage(Main.TEXT_INFO + args[1] + "をホワイトリストから削除しました");
                }
                else {
                    send.sendMessage(Main.TEXT_ERROR + "そのプレイヤーは登録されていません。");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (args[0].equalsIgnoreCase("list")) {
            String list = StringUtils.join(WhiteListManager.getPlayers(), ",");
            if (list.startsWith(",")) {
                list = list.replaceFirst(",", "");
            }
            send.sendMessage(Main.TEXT_INFO + "参加できるプレイヤー：" + list);
        }
        return false;
    }

}
