package org.davincischools.leo.server;

import static org.davincischools.leo.server.SpringConstants.LOCAL_SERVER_PORT_PROPERTY;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.davincischools.leo.database.daos.UserX;
import org.davincischools.leo.database.post_environment_processors.LoadCustomProjectLeoProperties;
import org.davincischools.leo.database.test.TestDatabase;
import org.davincischools.leo.database.utils.Database;
import org.davincischools.leo.protos.pl_types.User;
import org.davincischools.leo.server.utils.http_user.HttpUserArgumentResolver;
import org.davincischools.leo.server.utils.http_user.HttpUserService;
import org.davincischools.leo.server.utils.http_user.UserXDetails;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@SpringBootApplication(
    scanBasePackages = "org.davincischools.leo.server",
    scanBasePackageClasses = {
      Database.class,
      HttpUserService.class,
      TestDatabase.class,
      UserX.class
    })
public class ServerApplication {

  private static final Logger logger = LogManager.getLogger();

  @Configuration
  static class ServerApplicationConfigurer extends WebMvcConfigurationSupport {

    private final Database db;

    public ServerApplicationConfigurer(Database db) {
      this.db = db;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
      // Add the Protobuf converters to the list of converters.
      //
      // These need to be at the beginning of the list to take precedence over
      // other converters. Otherwise, a different, built-in converter fails
      // with a "Direct self-reference leading to cycle" error.
      converters.add(0, new ProtobufHttpMessageConverter());
      converters.add(1, new ProtobufJsonFormatHttpMessageConverter());
    }

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
      argumentResolvers.add(0, new HttpUserArgumentResolver(db));
    }
  }

  @Configuration
  @EnableWebSecurity
  static class SecurityConfigurer {

    @Bean
    public SecurityFilterChain buildSecurityFilterChain(HttpSecurity http) throws Exception {
      // https://docs.spring.io/spring-security/reference/5.8/migration/servlet/exploits.html#_i_am_using_angularjs_or_another_javascript_framework
      CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
      XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();

      return http

          // Prevent Cross-Site Request Forgery.
          .csrf(
              config ->
                  config
                      .csrfTokenRepository(tokenRepository)
                      .csrfTokenRequestHandler(
                          (HttpServletRequest request,
                              HttpServletResponse response,
                              Supplier<CsrfToken> csrfToken) -> {
                            // The CSRF cookie isn't actually generated until the get() method
                            // is called, because it's generation is lazy. For the build-in
                            // login page, get() is called as part of creating the login HTML.
                            // But, get() is not called if you use an external login page,
                            // like we are doing. So, in order to force a CSRF token to be
                            // generated we manually call get() here. Once it's generated, it
                            // is automatically included as a cookie in the response.
                            csrfToken.get();

                            // Use only the handle() method of XorCsrfTokenRequestAttributeHandler
                            // and the default implementation of resolveCsrfTokenValue() from
                            // CsrfTokenRequestHandler
                            delegate.handle(request, response, csrfToken);
                          }))

          // Login
          .formLogin(
              config ->
                  config
                      .loginPage("/users/login")
                      .loginProcessingUrl("/api/login")
                      .successHandler(
                          (HttpServletRequest request,
                              HttpServletResponse response,
                              Authentication authentication) -> {
                            // Return the user in the reply after authentication.
                            UserX userX = ((UserXDetails) authentication.getPrincipal()).getUserX();
                            User user =
                                User.newBuilder()
                                    .setId(userX.getId())
                                    .setDistrictId(userX.getDistrict().getId())
                                    .setEmailAddress(userX.getEmailAddress())
                                    .setFirstName(userX.getFirstName())
                                    .setLastName(userX.getLastName())
                                    .setIsAdmin(userX.getAdminX().getId() != null)
                                    .setIsTeacher(userX.getTeacher().getId() != null)
                                    .setIsStudent(userX.getStudent().getId() != null)
                                    .build();

                            response.setContentType(
                                ProtobufHttpMessageConverter.PROTOBUF.toString());
                            response.setHeader(
                                ProtobufHttpMessageConverter.X_PROTOBUF_SCHEMA_HEADER,
                                user.getDescriptorForType().getFile().getName());
                            response.setHeader(
                                ProtobufHttpMessageConverter.X_PROTOBUF_MESSAGE_HEADER,
                                user.getDescriptorForType().getFullName());
                            response.getOutputStream().write(user.toByteArray());
                            response.setStatus(HttpServletResponse.SC_OK);
                          })
                      .failureUrl("/users/login?failed=true")
                      .permitAll())

          // Logout
          .logout(
              config ->
                  config
                      .logoutUrl("/api/logout")
                      .logoutSuccessUrl("/")
                      .clearAuthentication(true)
                      .invalidateHttpSession(true)
                      .deleteCookies("JSESSIONID")
                      .permitAll())

          // A user's profile requires full authentication.
          .authorizeHttpRequests(
              config ->
                  config
                      .requestMatchers(new AntPathRequestMatcher("/users/my-account"))
                      .fullyAuthenticated())

          // Public content.
          .authorizeHttpRequests(
              config ->
                  config
                      .requestMatchers(
                          // This needs to be kept in sync with ReactResourceController.
                          new AntPathRequestMatcher("/", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/docs/**", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/favicon.*", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/images/**", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/index.html", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/manifest.json", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/robots.txt", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/static/**", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/users/logout", HttpMethod.GET.name()),
                          // TODO: Move these out of the prod server.
                          // React developer tools plugin.
                          new AntPathRequestMatcher("/installHooks.js", HttpMethod.GET.name()),
                          // Webpack server hot reload files:
                          // https://github.com/webpack/webpack-dev-server.
                          new AntPathRequestMatcher("/main.*.hot-update.js", HttpMethod.GET.name()),
                          new AntPathRequestMatcher(
                              "/main.*.hot-update.js.map", HttpMethod.GET.name()),
                          new AntPathRequestMatcher(
                              "/main.*.hot-update.json", HttpMethod.GET.name()))
                      .permitAll())

          // Remaining pages require authentication.
          .authorizeHttpRequests(
              config ->
                  config
                      .requestMatchers(
                          new AntPathRequestMatcher("/api/**", HttpMethod.POST.name()),
                          new AntPathRequestMatcher("/profiles/**", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/projects/**", HttpMethod.GET.name()),
                          new AntPathRequestMatcher("/users/**", HttpMethod.GET.name()))
                      .authenticated())

          // Deny everything else.
          .authorizeHttpRequests(config -> config.anyRequest().denyAll())

          // TODO: Set security realm.
          // .httpBasic(config -> config.realmName("project.leo"))

          // Done with configuration.
          .build();
    }
  }

  public static void main(String[] args) throws IOException {
    // Load custom Project Leo properties into the environment.
    ConfigurableEnvironment environment = new StandardEnvironment();
    LoadCustomProjectLeoProperties.loadCustomProjectLeoProperties(environment);

    // Start the Project Leo server.
    SpringApplication sa = new SpringApplication(ServerApplication.class);
    sa.setEnvironment(environment);
    ApplicationContext context = sa.run(args);

    // Dump the list of beans in the context.
    logger.atInfo().log("Bean names available:");
    String[] beanNames = context.getBeanDefinitionNames();
    Arrays.sort(beanNames);
    for (String beanName : beanNames) {
      logger.atInfo().log("  - {}", beanName);
    }

    // Log the port that the server is running on.
    int serverPort =
        context.getEnvironment().getProperty(LOCAL_SERVER_PORT_PROPERTY, Integer.class, 0);
    logger.atInfo().log("Leo server started on port http://localhost:{}.", serverPort);
  }
}
