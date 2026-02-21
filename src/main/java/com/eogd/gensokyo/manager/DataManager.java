package com.eogd.gensokyo.manager;

import com.eogd.gensokyo.GensokyoSocialPlugin;
import com.eogd.gensokyo.data.Mail;
import com.eogd.gensokyo.data.Party;
import com.eogd.gensokyo.data.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {

    private final GensokyoSocialPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> cachedPlayerData;
    // 活跃队伍
    private final Map<UUID, Party> activeParties; 

    public DataManager(GensokyoSocialPlugin plugin) {
        this.plugin = plugin;
        this.cachedPlayerData = new HashMap<>();
        this.activeParties = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData loadPlayerData(UUID uuid) {
        if (cachedPlayerData.containsKey(uuid)) {
            return cachedPlayerData.get(uuid);
        }

        File userFile = new File(dataFolder, uuid.toString() + ".yml");
        PlayerData data = new PlayerData(uuid);

        if (userFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);
            
            // 读好友
            List<String> friendStrings = config.getStringList("friends");
            for (String f : friendStrings) {
                data.addFriend(UUID.fromString(f));
            }

            // 读邮件
            if (config.contains("mails")) {
                for (String key : config.getConfigurationSection("mails").getKeys(false)) {
                    String sender = config.getString("mails." + key + ".sender");
                    String content = config.getString("mails." + key + ".content");
                    long timestamp = config.getLong("mails." + key + ".timestamp");
                    data.addMail(new Mail(sender, content, timestamp));
                }
            }
        } else {
            // 欢迎信
            String sender = plugin.getConfig().getString("settings.system-mail-sender", "Kirisame Marisa");
            data.addMail(new Mail(sender, "欢迎来到幻想乡！把背后的防线交给你的队伍吧，祝你搜打撤武运昌隆！", System.currentTimeMillis()));
            savePlayerData(data);
        }

        cachedPlayerData.put(uuid, data);
        return data;
    }

    public void savePlayerData(PlayerData data) {
        File userFile = new File(dataFolder, data.getUuid().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);

        List<String> friendStrings = new ArrayList<>();
        for (UUID f : data.getFriends()) {
            friendStrings.add(f.toString());
        }
        config.set("friends", friendStrings);

        // 覆写邮件
        config.set("mails", null); 
        int index = 0;
        for (Mail mail : data.getMails()) {
            config.set("mails." + index + ".sender", mail.getSender());
            config.set("mails." + index + ".content", mail.getContent());
            config.set("mails." + index + ".timestamp", mail.getTimestamp());
            index++;
        }

        try {
            config.save(userFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存玩家数据: " + data.getUuid().toString());
            e.printStackTrace();
        }
    }

    public void unloadPlayerData(UUID uuid) {
        if (cachedPlayerData.containsKey(uuid)) {
            savePlayerData(cachedPlayerData.get(uuid));
            cachedPlayerData.remove(uuid);
        }
    }

    public PlayerData getCachedData(UUID uuid) {
        return cachedPlayerData.get(uuid);
    }

    public Map<UUID, Party> getActiveParties() {
        return activeParties;
    }
}