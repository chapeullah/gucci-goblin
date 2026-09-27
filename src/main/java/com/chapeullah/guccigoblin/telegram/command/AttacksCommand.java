package com.chapeullah.guccigoblin.telegram.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttacksCommand implements BotCommand{

    @Override
    public String name() {
        return "/attacks";
    }

    @Override
    public String execute(String args) {
        return "Неиспользованные атаки";
    }

}
