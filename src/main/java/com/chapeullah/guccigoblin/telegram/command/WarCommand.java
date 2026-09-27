package com.chapeullah.guccigoblin.telegram.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarCommand implements BotCommand{

    @Override
    public String name() {
        return "/war";
    }

    @Override
    public String execute(String args) {
        return "Текущая война";
    }

}
