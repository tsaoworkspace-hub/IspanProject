package com.project.sneakermarket.notification.controller;

import com.project.sneakermarket.notification.dto.NotificationLogDTO;
import com.project.sneakermarket.notification.service.NotificationLogService;
import com.project.sneakermarket.common.security.SessionAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationLogController {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationLogController.class);

    @Autowired
    private NotificationLogService notificationLogService;

    @Autowired
    private SessionAuthorizationService authorizationService;

    @GetMapping("/api/notificationLog/searchlogs")
    public ResponseEntity<?> getNotificationLogs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String sendType,
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {

        authorizationService.requireAdmin(httpRequest);

        try {
            Page<NotificationLogDTO> result = notificationLogService.searchLogs(title, sendType, pageNumber, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查詢通知紀錄失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("伺服器暫時無法處理請求");
        }
    }
}
