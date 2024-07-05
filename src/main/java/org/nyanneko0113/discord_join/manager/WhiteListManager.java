package org.nyanneko0113.discord_join.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nyanneko0113.discord_join.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhiteListManager {

    public static void addPlayer(String name) throws IOException {
        createJson();

        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            JsonObject players = new Gson().fromJson(reader, JsonObject.class);
            players.getAsJsonArray("players").add(name);

            try (BufferedWriter write = new BufferedWriter(new FileWriter(getFile()))) {
                write.write(players.toString());
            }
        }
    }

    public static void removePlayer(String name) throws IOException {
        createJson();

        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            JsonObject players = new Gson().fromJson(reader, JsonObject.class);
            JsonArray array = players.getAsJsonArray("players");

            for (int n = 0; n < array.size(); n++) {
                if (array.get(n).getAsString().equalsIgnoreCase(name)) {
                    array.remove(n);
                }
            }

            try (BufferedWriter write = new BufferedWriter(new FileWriter(getFile()))) {
                write.write(players.toString());
            }
        }
    }

    public static Set<String> getPlayers() {
        if (getFile().exists()) {
            JsonObject json = getJson();

            JsonArray players = json.getAsJsonArray("players");
            Set<String> set = new HashSet<>();
            for (int n = 0; n < players.size(); n++) {
                String mute = players.get(n).getAsString();

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
