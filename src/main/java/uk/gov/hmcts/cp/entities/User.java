package uk.gov.hmcts.cp.entities;

public record User(
        String userId,
        String firstName,
        String lastName,
        String email
) {}
