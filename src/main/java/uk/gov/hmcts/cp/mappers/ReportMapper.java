package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.output.Report;
import uk.gov.hmcts.cp.openapi.model.AuditReportListingItem;

import java.net.URI;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ReportMapper extends Converter<Report, AuditReportListingItem> {

    default URI stringToUri(String text) {
        return URI.create(text);
    }
}
