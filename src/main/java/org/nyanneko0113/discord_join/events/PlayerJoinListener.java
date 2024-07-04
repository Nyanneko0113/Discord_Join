package org.nyanneko0113.discord_join.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.nyanneko0113.discord_join.manager.WhiteListManager;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        if (!WhiteListManager.getPlayers().contains(event.getPlayer().getName())) {
            event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            event.setKickMessage("あなたはホワイトリストに登録されていません!" + "\n" + "\n" +
                    "【参加方法】Discord（https://discord.gg/8Xzr4rqUFa）に参加し、/server-join <id>　と入力してください!" + "\n" +
                    "（入力例：/server-join Blockgrass）");
        }
        else {
            event.allow();
        }
    }

}
