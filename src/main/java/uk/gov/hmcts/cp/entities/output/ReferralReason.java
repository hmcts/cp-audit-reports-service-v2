package uk.gov.hmcts.cp.entities.output;

public record ReferralReason( String id,
                                      String seqId,
                                      String hearingCode,
                                      String reason,
                                      String reasonCode,
                                      String welshReason) {
}