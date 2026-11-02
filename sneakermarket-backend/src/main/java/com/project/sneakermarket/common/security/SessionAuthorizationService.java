package com.project.sneakermarket.common.security;

import com.project.sneakermarket.common.exception.ForbiddenException;
import com.project.sneakermarket.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SessionAuthorizationService {

    private final MemberService memberService;

    public Long requireMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw unauthorized();
        }

        Object value = session.getAttribute("memberId");
        if (!(value instanceof Long memberId) || memberId <= 0) {
            throw unauthorized();
        }

        return memberId;
    }

    public Long requireAdmin(HttpServletRequest request) {
        Long memberId = requireMember(request);

        if (!memberService.isAdmin(memberId)) {
            throw new ForbiddenException("需要管理員權限");
        }

        return memberId;
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "尚未登入"
        );
    }
}
