package com.chapeullah.guccigoblin.telegram.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopCommand implements BotCommand{

    @Override
    public String name() {
        return "/top";
    }

    @Override
    public String execute(String args) {
        return "Рейтинг";
    }

}
