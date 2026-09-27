package com.chapeullah.guccigoblin.telegram.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements BotCommand{

    @Override
    public String name() {
        return "/help";
    }

    @Override
    public String execute(String args) {
        return "Помощь";
    }

}
