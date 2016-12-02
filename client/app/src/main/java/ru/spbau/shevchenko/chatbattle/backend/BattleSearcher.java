package ru.spbau.shevchenko.chatbattle.backend;

import ru.spbau.shevchenko.chatbattle.Player;

public class BattleSearcher {
    public static void findBattle(final Player.Role role) {
        RequestMaker.findBattle(role, ProfileManager.getPlayer().getId());
    }
}
