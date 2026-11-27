package uk.gov.hmcts.cp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.services.ReportBundleService;

import java.io.IOException;

@Slf4j
@RestController
@ConditionalOnProperty(name = "authz.http.enabled", havingValue = "true")
public record ReportBundleController(ReportBundleService bundles) {
    @GetMapping(value = "/reports/bundle", produces = "application/zip")
    public ResponseEntity<byte[]> download(
            @RequestParam("reference") final String reference,
            @RequestHeader("CJSCPPUID") final String caller
    ) throws IOException {
        final byte[] bundle;
        bundle = bundles.download(reference);
        log.info("Restricted audit workbook bundle issued for report {} to caller {}", reference, caller);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("audit-report-" + reference + "-workbooks.zip").build().toString())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(bundle);
    }
}
