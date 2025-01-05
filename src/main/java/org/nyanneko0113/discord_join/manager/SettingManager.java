package org.nyanneko0113.discord_join.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nyanneko0113.discord_join.Main;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class SettingManager {

    private static final Role role = null;
    private static final URL inv_url = null;
    private static final int MAX_PLAYER = 5;
    private static final @NotNull FileConfiguration config = Main.getInstance().getConfig();

    public static String getBanRoleID() {
        return config.getString("ban_role");
    }
    
    public static void setBanRole(Role role) {
        @NotNull FileConfiguration config = Main.getInstance().getConfig();
        config.set("ban_role", role.getId());
        Main.getInstance().saveConfig();
    }

    public static int getMaxPlayer() {
        if (config.get("max_verifyplayer") != null) {
            return config.getInt("max_verifyplayer");
        }
        else {
            config.getInt("max_verifyplayer");
        }
        return 0;
    }

    public static void setMaxPlayer(int player) {
        if (player > 0) {
            config.set("max_verifyplayer", player);
            Main.getInstance().saveConfig();
        }
        else {
            throw new IllegalArgumentException("0以下は設定できません。");
        }
    }

    public static String getInvite() {
        return config.getString("invite_url");
    }

    public static void setInvite(String url) {
        config.set("invite_url", url);
        Main.getInstance().saveConfig();
    }
    
}
