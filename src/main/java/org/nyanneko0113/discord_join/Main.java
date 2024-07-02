package org.nyanneko0113.discord_join;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import javax.security.auth.login.LoginException;

public final class Main extends JavaPlugin {

    private static final String TEXT_INFO = ChatColor.AQUA + "[DiscordJoin] " + ChatColor.RESET;
    private static final String TEXT_ERROR = ChatColor.RED + "[エラー] " + ChatColor.RESET;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            DiscordUtil.startBot();
        } catch (LoginException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        getLogger().info("プラグインが起動しました");
    }

    @Override
    public void onDisable() {
        DiscordUtil.stopBot();
    }

    public static Main getInstance() {
        return getPlugin(Main.class);
    }

}
