package uk.gov.hmcts.cp.entities;

import uk.gov.hmcts.cp.entities.output.Case;

import java.util.List;

public record Cases(List<Case> systemIds) { }
