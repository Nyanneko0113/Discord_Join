package org.nyanneko0113.discord_join.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.nyanneko0113.discord_join.manager.VerifyManager;
import org.nyanneko0113.discord_join.manager.WhiteListManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        @NotNull Player player = event.getPlayer();
        if (!WhiteListManager.getPlayers().contains(player.getName())) {
            VerifyManager.startVerify(player);

            VerifyManager.Verify verify = VerifyManager.getVerify(player);
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage("あなたはホワイトリストに登録されていません!" + "\n" + "\n" +
                    "【参加方法】Discord（https://discord.gg/8Xzr4rqUFa）に参加し、以下の認証コードを入力してください" + "\n" +
                    ChatColor.YELLOW + "認証コード：" + ChatColor.RESET + verify.getCode() + "\n" +
                    "（認証有効期限：" + getDateString(verify.getDate()) + ")");
        }
        else {
            event.allow();
        }
    }

    private String getDateString(Date date) {
        LocalDateTime datetime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return datetime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
    }

}
