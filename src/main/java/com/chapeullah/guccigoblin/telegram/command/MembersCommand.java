package com.chapeullah.guccigoblin.telegram.command;

import com.chapeullah.guccigoblin.member.Member;
import com.chapeullah.guccigoblin.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MembersCommand implements BotCommand {

    private final MemberRepository memberRepository;

    @Override
    public String name() {
        return "/members";
    }

    @Override
    public String execute(String args) {
        List<Member> members = memberRepository
                .findAllByInClanTrue()
                .stream()
                .sorted(Comparator.comparing(Member::getClanRank))
                .toList();
        if (members.isEmpty()) {
            return "Участники клана не найдены.";
        }
        StringBuilder result = new StringBuilder()
                .append("Участники клана ")
                .append(members.size())
                .append("\n");
        for (Member member : members) {
            result.append("\n")
                    .append(member.getClanRank())
                    .append(" — <code>")
                    .append(member.getTag())
                    .append("</code> — <code>")
                    .append(member.getName())
                    .append("</code> — TH")
                    .append(member.getTownHallLevel());
        }
        return result.toString();
    }

}
