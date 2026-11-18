package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.output.Report;
import uk.gov.hmcts.cp.openapi.model.AuditReportListingItem;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ReportMapper extends Converter<Report, AuditReportListingItem> {

    default URI stringToUri(final String text) {
        return text == null || text.isBlank() ? null : URI.create(text);
    }

    default Instant stringToInstant(final String text) {
        // Pending updates can leave an empty end time; historical rows may be incomplete.
        Optional<Instant> result;
        try {
            result = text == null || text.isBlank() ? Optional.empty() : Optional.of(Instant.parse(text));
        } catch (DateTimeParseException ignored) {
            result = Optional.empty();
        }
        return result.orElse(null);
    }
}
