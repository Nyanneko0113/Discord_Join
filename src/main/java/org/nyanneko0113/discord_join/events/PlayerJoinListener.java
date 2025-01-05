package org.nyanneko0113.discord_join.events;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.ban.BanListType;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nyanneko0113.discord_join.manager.SettingManager;
import org.nyanneko0113.discord_join.manager.VerifyManager;
import org.nyanneko0113.discord_join.manager.WhiteListManager;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerLoginEvent event) throws URISyntaxException, MalformedURLException {
        @NotNull Player player = event.getPlayer();
        if (!WhiteListManager.getPlayers().contains(player.getName())) {
            VerifyManager.startVerify(player);

            VerifyManager.Verify verify = VerifyManager.getVerify(player);
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);

            if (SettingManager.getInvite() != null) {
                event.setKickMessage("あなたはホワイトリストに登録されていません!" + "\n" + "\n" +
                        "【参加方法】Discord(" + SettingManager.getInvite() +")に参加し、以下の認証コードを入力してください" + "\n" +
                        ChatColor.YELLOW + "認証コード：" + ChatColor.RESET + verify.getCode() + "\n" +
                        "（認証有効期限：" + getDateString(verify.getDate()) + ")");
            }
            else {
                Bukkit.getLogger().warning("DiscordのURLが設定されていません!");
                event.setKickMessage("あなたはホワイトリストに登録されていません!" + "\n" + "\n" +
                        "【参加方法】Discordに参加し、以下の認証コードを入力してください" + "\n" +
                        ChatColor.YELLOW + "認証コード：" + ChatColor.RESET + verify.getCode() + "\n" +
                        "（認証有効期限：" + getDateString(verify.getDate()) + ")");
            }


        }
        else {
            @NotNull ProfileBanList name = Bukkit.getBanList(BanListType.PROFILE);
            BanList<?> ip = Bukkit.getBanList(BanListType.IP);
            @Nullable BanEntry<PlayerProfile> entry = name.getBanEntry(player.getPlayerProfile());

            if (name.isBanned(player.getPlayerProfile())) {
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            }
            else {
                event.allow();
            }
        }
    }

    private String getDateString(Date date) {
        LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return datetime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
    }

}
