package org.nyanneko0113.discord_join.manager;

import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.nyanneko0113.discord_join.Main;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VerifyManager {

    private static final List<Verify> verify_list = new ArrayList<>();

    public static void startVerify(OfflinePlayer player) {
        Date date = new Date();
        date.setMinutes(date.getMinutes() + 10);

        verify_list.add(new Verify(player, date));
    }

    public static int removeVerify(int code, User user) throws IOException {
        for (Verify verify : verify_list) {
            if (verify.getCode() == code) {
                int white_list = WhiteListManager.addPlayer(verify.getPlayer().getName(), user);
                if (white_list == 0) {
                    Bukkit.getLogger().info("[DiscordJoin] " + verify.getPlayer().getName() + "の認証が成功しました");

                    verify.stop();
                    verify_list.remove(verify);
                    return 0;
                }
                else if (white_list == 1){
                    return 1; //登録数上限
                }
            }
        }
        return 2; //番号が違う
    }

    public static Verify getVerify(OfflinePlayer player) {
        for (Verify verify : verify_list) {
            if (player.getName().equalsIgnoreCase(verify.getPlayer().getName())) {
                return verify;
            }
        }
        return null;
    }

    public static OfflinePlayer getPlayer(int code) {
        for (Verify verify : verify_list) {
            if (verify.getCode() == code) {
                return verify.getPlayer();
            }
        }
        return null;
    }

    public static class Verify {

        private final OfflinePlayer player;
        private final Date date;
        private final int code;
        private final BukkitTask task;

        public Verify(OfflinePlayer player, Date date) {
            this.player = player;
            this.date = date;

            Random random = new Random();
            this.code = random.nextInt(10000) + 1000;

            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() > date.getTime()) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 20L);
        }

        public OfflinePlayer getPlayer() {
            return player;
        }

        public Date getDate() {
            return date;
        }

        public int getCode() {
            return code;
        }

        private void stop() {
            task.cancel();
        }
    }
}
