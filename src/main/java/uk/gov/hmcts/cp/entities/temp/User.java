package uk.gov.hmcts.cp.entities.temp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(String userId, String firstName, String lastName, String email) {}
