package com.project.sneakermarket.notification.controller;

import com.project.sneakermarket.notification.dto.NotificationCalendarDashboardDTO;

import com.project.sneakermarket.notification.service.NotificationCalendarDashboardService;
import com.project.sneakermarket.common.security.SessionAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class NotificationCalendarDashboardController {

    private final NotificationCalendarDashboardService dashboardService;
    private final SessionAuthorizationService authorizationService;

    /**
     * 取得通知與行事曆後台首頁統計。
     */
    @GetMapping("/notification-calendar")
    public ResponseEntity<NotificationCalendarDashboardDTO>
    getNotificationCalendarDashboard(HttpServletRequest httpRequest) {

        authorizationService.requireAdmin(httpRequest);

        NotificationCalendarDashboardDTO dashboard =
                dashboardService.getDashboard();

        return ResponseEntity.ok(dashboard);
    }
}
