package com.eogd.gensokyo.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final List<UUID> friends;
    private final List<Mail> mails;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.friends = new ArrayList<>();
        this.mails = new ArrayList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<UUID> getFriends() {
        return friends;
    }

    public void addFriend(UUID friendUuid) {
        if (!friends.contains(friendUuid)) {
            friends.add(friendUuid);
        }
    }

    public void removeFriend(UUID friendUuid) {
        friends.remove(friendUuid);
    }

    public List<Mail> getMails() {
        return mails;
    }

    public void addMail(Mail mail) {
        mails.add(mail);
    }

    public void clearMails() {
        mails.clear();
    }
}