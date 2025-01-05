package org.nyanneko0113.discord_join.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nyanneko0113.discord_join.Main;
import org.nyanneko0113.discord_join.util.DiscordUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhiteListManager {

    private static final int MAX_PLAYER = 5;


    /**
     * ホワイトリストからプレイヤーを追加するコマンド
     * @param name MinecraftのID
     * @param user DiscordのID
     */
    public static int addPlayer(String name, User user) throws IOException {
        createJson();

        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            JsonObject players = new Gson().fromJson(reader, JsonObject.class);

            if (!(getUsers(name).size() >= MAX_PLAYER || getPlayers(user).size() >= MAX_PLAYER)) {
                JsonArray array = players.getAsJsonArray("players");

                JsonObject player = new JsonObject();
                player.addProperty("name", name);
                player.addProperty("discord_id", user.getId());

                array.add(player);

                try (BufferedWriter write = new BufferedWriter(new FileWriter(getFile()))) {
                    write.write(players.toString());
                }
                return 0;
            }
            else {
                return 1; //登録数上限
            }
        }
    }


    /**
     * ホワイトリストからプレイヤーを削除するコマンド
     * @param name MinecraftのID
    */
    public static void removePlayer(String name) throws IOException {
        createJson();

        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            JsonObject players = new Gson().fromJson(reader, JsonObject.class);
            JsonArray array = players.getAsJsonArray("players");

            for (int n = 0; n < array.size(); n++) {
                JsonObject player = array.get(n).getAsJsonObject();
                if (player.get("name").getAsString().equalsIgnoreCase(name)) {
                    array.remove(n);
                    break;
                }
            }

            try (BufferedWriter write = new BufferedWriter(new FileWriter(getFile()))) {
                write.write(players.toString());
            }
        }
    }

    /**
     * MinecraftのIDから、DiscordのUserを返す
     * @param name MinecraftのID
     * @return DiscordのUser
     */
    public static User getUser(String name) {
        if (getFile().exists()) {
            if (getFile().exists()) {
                JsonObject json = getJson();

                JsonArray players = json.getAsJsonArray("players");
                for (int n = 0; n < players.size(); n++) {
                    JsonObject player = players.get(n).getAsJsonObject();
                    Bukkit.broadcastMessage(player.get("name").getAsString().equalsIgnoreCase(name) +
                            "\n" + player.get("discord_id").getAsString());
                    if (player.get("name").getAsString().equalsIgnoreCase(name)) {
                        String mute = player.get("discord_id").getAsString();
                        return DiscordUtil.getJda().retrieveUserById(mute).complete();
                    }
                }
            }
            else {
                return null;
            }
        }
        else {
            throw new NullPointerException();
        }
        return null;
    }


    /**
     * MinecraftのIDから、登録されてるDiscordのUserを全て返す
     * @param name MinecraftのID
     * @return DiscordのUserのSet
     */
    public static Set<User> getUsers(String name) {
        if (getFile().exists()) {
            if (getFile().exists()) {
                JsonObject json = getJson();

                JsonArray players = json.getAsJsonArray("players");
                Set<User> set = new HashSet<>();
                for (int n = 0; n < players.size(); n++) {
                    JsonObject player = players.get(n).getAsJsonObject();
                    if (player.get("name").getAsString().equalsIgnoreCase(name)) {
                        String mute = player.get("discord_id").getAsString();
                        set.add(DiscordUtil.getJda().retrieveUserById(mute).complete());
                    }
                }
                return set;
            }
            else {
                return null;
            }
        }
        else {
            throw new NullPointerException();
        }
    }


    /**
     * DiscordのIDから、登録されてるMinecraftのIDを全て返す
     * @param user DiscordのUser
     * @return PlayerのSet
     */
    public static Set<OfflinePlayer> getPlayers(User user) {
        if (getFile().exists()) {
            if (getFile().exists()) {
                JsonObject json = getJson();

                JsonArray players = json.getAsJsonArray("players");
                Set<OfflinePlayer> set = new HashSet<>();
                for (int n = 0; n < players.size(); n++) {
                    JsonObject player = players.get(n).getAsJsonObject();
                    if (player.get("discord_id").getAsString().equalsIgnoreCase(user.getId())) {
                        String mute = player.get("name").getAsString();
                        set.add(Bukkit.getOfflinePlayer(mute));
                    }
                }
                return set;
            }
            else {
                return null;
            }
        }
        else {
            throw new NullPointerException();
        }
    }

    /**
     * ホワイトリストに登録されている全プレイヤー
     * @return 登録されているプレイヤーの名前
     */
    public static Set<String> getPlayers() {
        if (getFile().exists()) {
            JsonObject json = getJson();

            JsonArray players = json.getAsJsonArray("players");
            Set<String> set = new HashSet<>();
            for (int n = 0; n < players.size(); n++) {
                JsonObject player = players.get(n).getAsJsonObject();
                String mute = player.get("name").getAsString();

                set.add(mute);
            }
            return set;
        }
        else {
            return new HashSet<>();
        }
    }

    public static JsonObject getJson() {
        if (getFile().exists()) {
            StringBuilder file_read = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    file_read.append(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new JsonParser().parse(file_read.toString()).getAsJsonObject();
        }
        else {
            throw new NullPointerException("ファイルが存在しません!");
        }
    }

    private static void createJson() throws IOException {
        if (!getFile().exists()) {
            try (BufferedWriter write = new BufferedWriter(new FileWriter(getFile()))) {
                JsonObject json = new JsonObject();
                json.add("players", new JsonArray());
                write.write(json.toString());
            }
        }
    }

    private static File getFile() {
        File path = Main.getInstance().getDataFolder();
        if (!path.exists()) {
            new File(path.getPath()).mkdir();
        }
        return new File(  path.getPath() + "/discord_whitelist.json");
    }
}
