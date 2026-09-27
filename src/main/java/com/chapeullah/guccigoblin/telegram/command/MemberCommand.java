package com.chapeullah.guccigoblin.telegram.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCommand implements BotCommand{

    @Override
    public String name() {
        return "/member";
    }

    @Override
    public String execute(String args) {
        return "Участник";
    }

}
