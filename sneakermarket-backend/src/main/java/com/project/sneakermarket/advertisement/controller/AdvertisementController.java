package com.project.sneakermarket.advertisement.controller;

import com.project.sneakermarket.advertisement.entity.AdvertisementPlacement;
import com.project.sneakermarket.advertisement.service.AdvertisementImageStorageService;
import com.project.sneakermarket.advertisement.dto.AdvertisementDeliveryStatus;
import com.project.sneakermarket.advertisement.dto.AdvertisementResponse;
import com.project.sneakermarket.advertisement.dto.CreateAdvertisementRequest;
import com.project.sneakermarket.advertisement.dto.UpdateAdvertisementRequest;
import com.project.sneakermarket.advertisement.dto.UpdateAdvertisementStatusRequest;
import com.project.sneakermarket.advertisement.dto.AdvertisementStatsResponse;
import com.project.sneakermarket.advertisement.entity.Advertisement;
import com.project.sneakermarket.advertisement.entity.AdvertisementStatus;
import com.project.sneakermarket.advertisement.service.AdvertisementService;
import com.project.sneakermarket.advertisement.service.AdvertisementStatsService;
import com.project.sneakermarket.common.response.ApiResponse;
import com.project.sneakermarket.common.security.SessionAuthorizationService;
import com.project.sneakermarket.advertisement.dto.AdvertisementDashboardSummaryResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService adService;
    private final AdvertisementImageStorageService adImageService;
    private final AdvertisementStatsService adStatsService;
    private final SessionAuthorizationService authorizationService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Long>> insertAdvertisement(
            @Valid @RequestBody CreateAdvertisementRequest request,
            HttpServletRequest httpRequest) {

        authorizationService.requireAdmin(httpRequest);

        Advertisement advertisement = adService.insertAdvertisement(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("廣告新增成功，尚未上架", advertisement.getAdId()));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<String>> uploadAdvertisementImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("adPlacement") AdvertisementPlacement adPlacement,
            HttpServletRequest httpRequest) {

        authorizationService.requireAdmin(httpRequest);

        String imageUrl = adImageService.store(file, adPlacement);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("廣告圖片上傳成功", imageUrl));
    }

    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<List<AdvertisementResponse>>> getAllAdvertisements(
            HttpServletRequest httpRequest) {
        authorizationService.requireAdmin(httpRequest);
        List<AdvertisementResponse> advertisements = adService.getAllAdvertisements();

        return ResponseEntity.ok(
                ApiResponse.ok("廣告列表查詢成功", advertisements)
        );
    }

    @DeleteMapping("/delete/{adId}")
    public ResponseEntity<ApiResponse<Void>> deleteAdvertisement(
            @PathVariable Long adId,
            HttpServletRequest httpRequest) {
        authorizationService.requireAdmin(httpRequest);
        adService.deleteAdvertisement(adId);

        return ResponseEntity.ok(ApiResponse.ok("廣告刪除成功",null));

    }

    @PutMapping("/put/{adId}")
    public ResponseEntity<ApiResponse<Long>> updateAdvertisement(
                @PathVariable Long adId,
                @Valid @RequestBody UpdateAdvertisementRequest request,
                HttpServletRequest httpRequest){
            authorizationService.requireAdmin(httpRequest);
            Advertisement advertisement = adService.updateAdvertisement(adId,request);

            return ResponseEntity.ok(ApiResponse.ok("廣告修改成功", advertisement.getAdId()));
    }

    @GetMapping("/findPage")
    public ResponseEntity<ApiResponse<Page<AdvertisementResponse>>> findAdByPage(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false) AdvertisementStatus adStatus,
            @RequestParam(required = false) AdvertisementDeliveryStatus deliveryStatus,
            @RequestParam(required = false) AdvertisementPlacement adPlacement,
            @RequestParam(required = false) String keyword,
            HttpServletRequest httpRequest){

        authorizationService.requireAdmin(httpRequest);

        Page<AdvertisementResponse> result =
                adService.findAdByPage(pageNumber, adStatus, deliveryStatus, adPlacement, keyword);

        return ResponseEntity.ok(
                ApiResponse.ok("廣告查詢成功", result)
        );
    }

    @PutMapping("/put/{adId}/status")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> updateAdvertisementStatus(
            @PathVariable Long adId,
            @Valid @RequestBody UpdateAdvertisementStatusRequest request,
            HttpServletRequest httpRequest){
        authorizationService.requireAdmin(httpRequest);
        AdvertisementResponse adResponse = adService.updateAdvertisementStatus(adId, request);

        return ResponseEntity.ok(ApiResponse.ok("廣告狀態更新成功",adResponse));
    }

    @GetMapping("/findActive")
    public ResponseEntity<ApiResponse<List<AdvertisementResponse>>>
    getPublicActiveAdvertisements() {

        List<AdvertisementResponse> advertisements =
                adService.getPublicActiveAdvertisements();

        return ResponseEntity.ok(
                ApiResponse.ok("公開廣告查詢成功", advertisements)
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<List<AdvertisementStatsResponse>>>
    getAdvertisementStats(
            @RequestParam("adIds") List<Long> adIds,
            HttpServletRequest httpRequest
    ) {
        authorizationService.requireAdmin(httpRequest);
        List<AdvertisementStatsResponse> stats =
                adStatsService.getTotalsByAdvertisementIds(adIds);

        return ResponseEntity.ok(
                ApiResponse.ok("廣告成效查詢成功", stats)
        );
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<ApiResponse<AdvertisementDashboardSummaryResponse>>
    getAdvertisementDashboardSummary(HttpServletRequest httpRequest) {
        authorizationService.requireAdmin(httpRequest);
        AdvertisementDashboardSummaryResponse summary =
                adStatsService.getDashboardSummary();

        return ResponseEntity.ok(
                ApiResponse.ok("廣告儀表板摘要查詢成功", summary)
        );
    }
}
