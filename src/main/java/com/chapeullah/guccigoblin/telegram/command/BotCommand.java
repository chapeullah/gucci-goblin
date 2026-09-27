package com.chapeullah.guccigoblin.telegram.command;

public interface BotCommand {

    String name();

    String execute(String args);

}
