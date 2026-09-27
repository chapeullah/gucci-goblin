package com.chapeullah.guccigoblin.telegram.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarsCommand implements BotCommand{

    @Override
    public String name() {
        return "/wars";
    }

    @Override
    public String execute(String args) {
        return "Войны";
    }

}
