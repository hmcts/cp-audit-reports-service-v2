package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.output.ReferralReason;
import uk.gov.hmcts.cp.openapi.model.ReferenceDataSearchResult;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ReferralReasonMapper extends Converter<ReferralReason, ReferenceDataSearchResult> {
}