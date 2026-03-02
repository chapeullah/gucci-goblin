package com.chapeullah.GucciGoblin.command;

import com.chapeullah.GucciGoblin.service.GucciService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.Instant;

@ShellComponent
@RequiredArgsConstructor
public class MemberCommands {

    private final GucciService gucciService;

    @ShellMethod(
            key = "member la show",
            value = "Show member's last activity. Usage: member la show [-t | --tag] <tag>"
    )
    public Instant showMembersLA(
            @ShellOption(value = {"-t", "--tag"}, help = "Player's tag.") String tag
    ) {
        return gucciService.showMembersLastActivity(tag);
    }

    @ShellMethod(
            key = "member la update",
            value = "Update member's last activity. Usage: member la update [-t | --tag] <tag>"
    )
    public String updateMembersLA(
            @ShellOption(value = {"-t", "--tag"}, help = "Player's tag.") String tag
    ) {
        return gucciService.updateMembersLastActivity(tag) ? "OK" : "Member not found";
    }

}
