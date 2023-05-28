package org.davincischools.leo.server.utils.http_user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.davincischools.leo.database.daos.UserX;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

public class HttpUser {

  public static boolean isAdmin(UserX userX) {
    return userX.getAdminX() != null && userX.getAdminX().getId() != null;
  }

  public static boolean isTeacher(UserX userX) {
    return userX.getTeacher() != null && userX.getTeacher().getId() != null;
  }

  public static boolean isStudent(UserX userX) {
    return userX.getStudent() != null && userX.getStudent().getId() != null;
  }

  private final Optional<UserX> userX;
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final boolean authorized;

  HttpUser(
      Optional<UserX> userX,
      HttpServletRequest request,
      HttpServletResponse response,
      boolean requireAuthenticated) {
    checkArgument(userX.isEmpty() || userX.get().getId() != null);
    this.userX = checkNotNull(userX);
    this.request = checkNotNull(request);
    this.response = checkNotNull(response);
    this.authorized = !requireAuthenticated || userX.isPresent();
  }

  public boolean isNotAuthorized() {
    return !authorized;
  }

  public <T> T returnForbidden(T response) {
    checkArgument(!authorized);
    this.response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    return response;
  }

  public <T> T forwardToLogin(T response) throws IOException {
    this.response.sendRedirect("/users/login");
    return response;
  }

  public HttpUser logout() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    SecurityContextHolder.getContext().setAuthentication(null);
    return this;
  }

  public boolean isAdmin() {
    return userX.isPresent() && isAdmin(userX.get());
  }

  public boolean isTeacher() {
    return userX.isPresent() && isTeacher(userX.get());
  }

  public boolean isStudent() {
    return userX.isPresent() && isStudent(userX.get());
  }

  public UserX get() {
    return userX.get();
  }

  public boolean isPresent() {
    return userX.isPresent();
  }

  public boolean isEmpty() {
    return userX.isEmpty();
  }
}
