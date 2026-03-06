package uk.gov.hmcts.cp.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.function.Predicate.not;

public class AuthorisationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Object handler) throws Exception {

        final Supplier<String> cjsCppUid = () -> request.getHeader("CJSCPPUID");

        final AccessControl accessControl = switch (handler) {
            case HandlerMethod method -> method.getMethodAnnotation(AccessControl.class);
            default -> null;
        };

        return Optional.
                ofNullable(accessControl).
                map(_ -> isUserAuthorised(cjsCppUid.get())).
                filter(not(Boolean::booleanValue)).
                map(_ -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }).
                orElse(true);
    }

    private boolean isUserAuthorised(String cjsCppUid) {

        return Optional.
                ofNullable(cjsCppUid).
                map(_ -> cjsCppUid.contains("?")). // @TODO: Call Drools with User Id
                orElse(false);
    }
}
