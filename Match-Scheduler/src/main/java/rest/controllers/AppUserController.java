package rest.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rest.data.dto.AdminUserDto;
import rest.data.dto.UserDetailDto;
import rest.data.entities.AppRole;
import rest.data.entities.AppUser;
import rest.data.exceptions.EmailAlreadyRegisteredException;
import rest.data.exceptions.UserNotFoundByIdException;
import rest.data.requests.ChangePasswordRequest;
import rest.data.requests.RoleToUserRequest;
import rest.data.responses.ProcessResponse;
import rest.data.utilities.Utility;
import rest.services.AppUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("")
public class AppUserController {
    private final AppUserService userService;

    @PostMapping("register")
    public ResponseEntity<?> saveUser(@RequestBody AppUser user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/register").toUriString());
        try {
            return ResponseEntity.created(uri).body(userService.saveUser(user));
        } catch (EmailAlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("admin/create/admin")
    public ResponseEntity<?> saveAdmin(@RequestBody AppUser admin) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/admin").toUriString());
        try {
            return ResponseEntity.created(uri).body(userService.saveAdmin(admin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("admin/delete/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("admin/create/role")
    public ResponseEntity<?> saveRole(@RequestBody AppRole role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/role").toUriString());
        try {
            return ResponseEntity.created(uri).body(userService.saveRole(role));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("admin/assign/role")
    public ResponseEntity<?> assignRoleToUser(@RequestBody RoleToUserRequest request) {
        try {
            userService.assignRoleToUser(request.getId(), request.getRoleName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("admin/remove/role")
    public ResponseEntity<?> removeRoleFromUser(@RequestBody RoleToUserRequest request) {
        try {
            userService.removeRoleFromUser(request.getId(), request.getRoleName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("user/email")
    public ResponseEntity<AppUser> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok().body(userService.findByEmail(email));
    }

    @PostMapping("user/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            ProcessResponse response = this.userService.changePassword(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("user/id")
    public ResponseEntity<?> getUserEmail() {
        try {
            UserDetailDto userDetailDto = userService.getUserEmail();
            return ResponseEntity.ok().body(userDetailDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("admin/users")
    public ResponseEntity<List<AdminUserDto>> findAll() {
        return ResponseEntity.ok().body(userService.findAll());
    }

    @PatchMapping(path = "user/update/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody JsonPatch patch) {
        try {
            AppUser updatedUser = userService.updateUser(id, patch);
            return ResponseEntity.ok().body(updatedUser);
        } catch (JsonPatchException | JsonProcessingException exception) {
            return ResponseEntity.internalServerError().build();
        } catch (UserNotFoundByIdException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                response = Utility.writeResponseBodyRefreshToken(request, response, userService, refresh_token);
            } catch (Exception exception) {
                response = Utility.writeResponseBodyError(response, exception.getMessage());
            }
        } else {
            throw new RuntimeException("Refresh token is missing.");
        }
    }
}
