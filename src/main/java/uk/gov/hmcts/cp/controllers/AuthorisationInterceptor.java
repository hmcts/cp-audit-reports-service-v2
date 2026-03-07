package uk.gov.hmcts.cp.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.cp.controllers.UserType.GROUP_USER;
import static uk.gov.hmcts.cp.controllers.UserType.SYSTEM_USER;

public class AuthorisationInterceptor implements HandlerInterceptor {

    private static final Map<UserType, List<String>> userTypeIds = Map.of(
            GROUP_USER, List.of("alice", "bob"),
            SYSTEM_USER, List.of("fabric")
    );

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Object handler) throws Exception {

        final String userId = Optional.ofNullable(request.getHeader("CJSCPPUID")).orElse("");

        final AccessControl accessControl = switch (handler) {
            case HandlerMethod method -> method.getMethodAnnotation(AccessControl.class);
            default -> null;
        };

        return Optional.
                ofNullable(accessControl).
                map(AccessControl::value).
                map(Arrays::asList).
                map(isUserAuthorised(userId)).
                filter(not(Boolean::booleanValue)).
                map(_ -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }).
                orElse(true);
    }

    private static Function<List<UserType>, Boolean> isUserAuthorised(String userId) {

        return authorisedTypes -> authorisedTypes.stream().
                map(userTypeIds::get).anyMatch(userIds -> userIds.contains(userId));

    }
}
