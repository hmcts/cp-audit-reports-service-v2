package uk.gov.hmcts.cp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.cp.controllers.AuthorisationInterceptor;

@Configuration
public class AuthorisationConfig implements WebMvcConfigurer {

    @Autowired
    AuthorisationInterceptor authorisationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorisationInterceptor);
    }
}
