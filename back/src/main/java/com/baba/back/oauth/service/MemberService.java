package com.baba.back.oauth.service;

import com.baba.back.baby.domain.Baby;
import com.baba.back.baby.domain.IdConstructor;
import com.baba.back.baby.repository.BabyRepository;
import com.baba.back.oauth.domain.ColorPicker;
import com.baba.back.oauth.domain.JoinedMember;
import com.baba.back.oauth.domain.member.Member;
import com.baba.back.oauth.dto.MemberJoinRequest;
import com.baba.back.oauth.dto.MemberJoinResponse;
import com.baba.back.oauth.exception.JoinedMemberBadRequestException;
import com.baba.back.oauth.exception.JoinedMemberNotFoundException;
import com.baba.back.oauth.repository.JoinedMemberRepository;
import com.baba.back.oauth.repository.MemberRepository;
import com.baba.back.relation.domain.DefaultRelation;
import com.baba.back.relation.domain.Relation;
import com.baba.back.relation.domain.RelationGroup;
import com.baba.back.relation.repository.RelationRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JoinedMemberRepository joinedMemberRepository;
    private final BabyRepository babyRepository;
    private final RelationRepository relationRepository;
    private final ColorPicker<String> colorPicker;
    private final IdConstructor idConstructor;

    private final LocalDate now = LocalDate.now();

    public MemberJoinResponse join(MemberJoinRequest request, String memberId) {
        final JoinedMember joinedMember = joinedMemberRepository.findById(memberId)
                .orElseThrow(() -> new JoinedMemberNotFoundException(memberId + "는 로그인하지 않은 memberId 입니다."));
        validateJoinedMember(joinedMember);
        final Member member = saveMember(memberId, request);
        joinedMember.signUp();

        final List<Baby> babies = saveBabies(request);
        saveRelations(babies, member, request.getRelationName());

        return new MemberJoinResponse(true, "회원가입이 완료되었습니다.");
    }

    private void validateJoinedMember(JoinedMember joinedMember) {
        if (joinedMember.isSigned()) {
            throw new JoinedMemberBadRequestException(joinedMember.getId() + "는 이미 회원가입한 memberId 입니다.");
        }
    }

    private Member saveMember(String memberId, MemberJoinRequest request) {
        return memberRepository.save(Member.builder()
                .id(memberId)
                .name(request.getName())
                .introduction("")
                .iconName(request.getIconName())
                .colorPicker(colorPicker)
                .build());
    }

    private List<Baby> saveBabies(MemberJoinRequest request) {
        String babyId = idConstructor.createId();
        return request.getBabies().stream()
                .map(babyRequest -> babyRequest.toEntity(babyId, now))
                .map(babyRepository::save)
                .toList();
    }

    private void saveRelations(List<Baby> babies, Member member, String relationName) {
        Relation defaultRelation = Relation.builder()
                .member(member)
                .baby(babies.get(0))
                .relationName(relationName)
                .relationGroup(RelationGroup.FAMILY)
                .defaultRelation(DefaultRelation.DEFAULT)
                .build();

        relationRepository.save(defaultRelation);

        IntStream
                .range(1, babies.size())
                .mapToObj(babies::get)
                .map(baby -> Relation.builder()
                        .member(member)
                        .baby(baby)
                        .relationName(relationName)
                        .relationGroup(RelationGroup.FAMILY)
                        .defaultRelation(DefaultRelation.NOT_DEFAULT)
                        .build())
                .forEach(relationRepository::save);
    }
}
