package com.project.sneakermarket.commerce.payment;

import com.project.sneakermarket.commerce.dto.OrderDetailResponse;
import com.project.sneakermarket.commerce.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.HtmlUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    @Value("${ecpay.merchant-id}")
    private String merchantId;

    @Value("${ecpay.hash-key}")
    private String hashKey;

    @Value("${ecpay.hash-iv}")
    private String hashIv;

    @Value("${ecpay.aio-checkout-url}")
    private String aioCheckoutUrl;

    @Value("${ecpay.return-url}")
    private String returnUrl;

  
    private static final String CLIENT_BACK_URL_PREFIX = "http://localhost:5173/orders/";

    @GetMapping(value = "/api/payment/checkout/{orderId}", produces = MediaType.TEXT_HTML_VALUE)
    public String checkout(@PathVariable Long orderId, HttpServletRequest request) {

        Long memberId = requireLoggedInMemberId(request);
        OrderDetailResponse order = orderService.getOrderDetail(memberId, orderId);

        String merchantTradeNo = "SM" + String.format("%010d", orderId);
        String merchantTradeDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("MerchantTradeDate", merchantTradeDate);
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(order.getTotalAmount().intValue()));
        params.put("TradeDesc", "酷客 Kicks 訂單付款");
        params.put("ItemName", "訂單" + order.getOrderNumber());
        params.put("ReturnURL", returnUrl);
        params.put("ClientBackURL", CLIENT_BACK_URL_PREFIX + orderId + "?fromPayment=true");
        params.put("ChoosePayment", "Credit");
        params.put("EncryptType", "1");

        String checkMacValue = ECPayCheckMacValueUtil.generate(params, hashKey, hashIv);
        params.put("CheckMacValue", checkMacValue);

        StringBuilder html = new StringBuilder();
        html.append("<html><body onload=\"document.forms[0].submit()\">");
        html.append("<form method=\"post\" action=\"")
                .append(HtmlUtils.htmlEscape(aioCheckoutUrl))
                .append("\">");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type=\"hidden\" name=\"")
                    .append(HtmlUtils.htmlEscape(entry.getKey()))
                    .append("\" value=\"")
                    .append(HtmlUtils.htmlEscape(entry.getValue()))
                    .append("\">");
        }
        html.append("</form></body></html>");

        return html.toString();
    }

    @PostMapping("/api/payment/notify")
    public String notify(@RequestParam Map<String, String> allParams) {
        String receivedCheckMacValue = allParams.get("CheckMacValue");
        String computed = ECPayCheckMacValueUtil.generate(allParams, hashKey, hashIv);

        if (receivedCheckMacValue == null
                || !MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.US_ASCII),
                receivedCheckMacValue.getBytes(StandardCharsets.US_ASCII))) {
            return "0|CheckMacValue Error";
        }

        String rtnCode = allParams.get("RtnCode");
        String merchantTradeNo = allParams.get("MerchantTradeNo");

        if ("1".equals(rtnCode) && merchantTradeNo != null && merchantTradeNo.startsWith("SM")) {
            Long orderId = Long.parseLong(merchantTradeNo.substring(2));
            orderService.confirmPayment(orderId);
        }else if (merchantTradeNo != null && merchantTradeNo.startsWith("SM")) {
            Long orderId = Long.parseLong(merchantTradeNo.substring(2));
            orderService.cancelOrder(orderId);
        }

        return "1|OK";
    }

    private Long requireLoggedInMemberId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "尚未登入");
        }
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "尚未登入");
        }
        return memberId;
    }
}
