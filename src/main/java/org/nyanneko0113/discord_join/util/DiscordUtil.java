package org.nyanneko0113.discord_join.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.nyanneko0113.discord_join.Main;
import org.nyanneko0113.discord_join.commands.DiscordCommand;
import org.nyanneko0113.discord_join.discord_commands.Discord_Command;
import org.nyanneko0113.discord_join.discord_commands.Discord_SettingCommand;
import org.nyanneko0113.discord_join.discord_commands.Discord_WhiteListCommand;
import org.nyanneko0113.discord_join.manager.VerifyManager;
import org.nyanneko0113.discord_join.manager.WhiteListManager;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.stream.Collectors;

public class DiscordUtil extends ListenerAdapter implements EventListener {

        private static JDA jda;
        private static final String token = Main.getInstance().getConfig().getString("token");

        public static void startBot() throws LoginException, InterruptedException {
            if (jda == null) {
                jda = JDABuilder.createDefault(token)
                        .addEventListeners(new Discord_WhiteListCommand())
                        .addEventListeners(new Discord_SettingCommand())
                        .addEventListeners(new Discord_Command())
                        .build();
                jda.awaitReady();


                CommandListUpdateAction commands = jda.updateCommands();
                commands.addCommands(Commands.slash("server-join", "サーバーに参加するコマンド")
                                .addOption(OptionType.STRING, "code", "認証コード", false))

                        .addCommands(Commands.slash("whitelist-add", "ホワイトリストを追加するコマンド")
                                .addOption(OptionType.STRING, "mcid", "マイクラのID", false))

                        .addCommands(Commands.slash("whitelist-remove", "ホワイトリストから削除するコマンド")
                                .addOption(OptionType.STRING, "mcid", "マイクラのID", false))

                        .addCommands(Commands.slash("ban", "Banするコマンド")
                                .addOptions(new OptionData(OptionType.STRING, "mcid", "マイクラのID", false),
                                        new OptionData(OptionType.STRING, "ban_reason", "Banする理由", false)))

                        .addCommands(Commands.slash("setting_discordjoin", "設定するコマンド")
                                .addOptions(new OptionData(OptionType.STRING, "setting", "設定する内容", false),
                                        new OptionData(OptionType.STRING, "setting_args", "設定にしたい対する引数", false)))

                        .addCommands(Commands.slash("userinfo", "ユーザーの情報を設定するコマンド")
                                .addOption(OptionType.STRING, "mcid", "minecraftid", false))

                        .addCommands(Commands.slash("find_name", "発見するコマンド")
                                .addOptions(new OptionData(OptionType.STRING, "type", "discord / minecraft", false),
                                        new OptionData(OptionType.STRING, "name", "名前", false)))

                        .addCommands(Commands.slash("info", "このBOTの情報"))

                        .addCommands(Commands.slash("help", "ヘルプコマンド"))

                        .addCommands(Commands.slash("list", "参加できるユーザー"))
                        .queue();


                Bukkit.getLogger().info("[DiscordUtil] [情報] ボットが起動しました。");
                Bukkit.getLogger().info("[DiscordUtil] [情報] 利用可能なコマンド：" + commands.complete().stream().map(Command::getName).collect(Collectors.toSet()));
            }
        }

        public static JDA getJda() {
            return jda;
        }

        public static void stopBot() {
            if (jda != null) {
                jda.shutdownNow();
            }
            else {
                Bukkit.getLogger().info("[DiscordUtil] [エラー] ボットが起動されていないためシャットダウンできません。");
            }
        }

}
