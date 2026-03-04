package uk.gov.hmcts.cp.entities.output;

public record User(
        String userId,
        String firstName,
        String lastName,
        String email
) {}
