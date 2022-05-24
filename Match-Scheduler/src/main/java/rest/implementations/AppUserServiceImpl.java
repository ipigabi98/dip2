package rest.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rest.data.dto.AdminUserDto;
import rest.data.dto.UserDetailDto;
import rest.data.entities.AppRole;
import rest.data.entities.AppUser;
import rest.data.exceptions.*;
import rest.data.requests.ChangePasswordRequest;
import rest.data.responses.ErrorResponse;
import rest.data.responses.ProcessResponse;
import rest.mappers.interfaces.AppUserMapper;
import rest.repos.AppRoleRepo;
import rest.repos.AppUserRepo;
import rest.security.IAuthenticationFacade;
import rest.services.AppUserService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService, UserDetailsService {
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final AppUserRepo userRepo;
    private final AppRoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper userMapper;

    private final IAuthenticationFacade authenticationFacade;

    @Override
    public AppUser saveUser(AppUser user) throws EmailAlreadyRegisteredException {
        return saveUserByRole(user, ROLE_USER);
    }

    @Override
    public AppUser saveAdmin(AppUser user) throws EmailAlreadyRegisteredException {
        return saveUserByRole(user, ROLE_ADMIN);
    }

    @Override
    public void deleteUser(Long userId) throws UserNotFoundByIdException {
        userRepo.findById(userId).orElseThrow(UserNotFoundByIdException::new);
        this.userRepo.deleteById(userId);
    }

    @Override
    public AppRole saveRole(AppRole role) throws RoleAlreadyExistsException {
        AppRole existingRole = roleRepo.findByName(role.getName());
        if (existingRole != null) {
            throw new RoleAlreadyExistsException();
        }
        return roleRepo.save(role);
    }

    @Override
    public void assignRoleToUser(Long userId, String roleName) throws UserNotFoundByIdException {
        AppUser user = userRepo.findById(userId).orElseThrow(UserNotFoundByIdException::new);
        AppRole role = roleRepo.findByName(roleName);
        user.getRoles().add(role);
    }

    @Override
    public void removeRoleFromUser(Long userId, String roleName) throws UserNotFoundByIdException, RoleDoesNotExistException {
        AppUser user = userRepo.findById(userId).orElseThrow(UserNotFoundByIdException::new);
        AppRole role = roleRepo.findByName(roleName);
        if (role == null) {
            throw new RoleDoesNotExistException();
        }
        user.getRoles().remove(role);
    }

    @Override
    public AppUser findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public ProcessResponse changePassword(ChangePasswordRequest request) throws UserNotFoundByEmailException, PasswordsDoNotMatchException {
        AppUser user = this.checkUserExists();
        String expectedOlPasswordHash = user.getPassword();
        String actualOldPassword = request.getOldPassword();

        Boolean doPasswordsMatch = this.passwordEncoder.matches(actualOldPassword, expectedOlPasswordHash);
        if (!doPasswordsMatch) {
            throw new PasswordsDoNotMatchException("Old password is incorrect.");
        }

        String encodedNewPassword = this.passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        this.userRepo.save(user);
        return new ProcessResponse(true);
    }

    @Override
    public AppUser findById(Long id) throws UserNotFoundByIdException {
        return userRepo.findById(id).orElseThrow(UserNotFoundByIdException::new);
    }

    @Override
    public UserDetailDto getUserEmail() throws UserNotFoundByEmailException {
        AppUser user = checkUserExists();
        UserDetailDto userDetailDto = new UserDetailDto();
        userDetailDto.setEmail(user.getEmail());
        return userDetailDto;
    }

    @Override
    public List<AdminUserDto> findAll() {
        List<AdminUserDto> result = new ArrayList<>();
        for (AppUser user : userRepo.findAll()) {
            AdminUserDto userDto = this.userMapper.entityToDto(user);
            result.add(userDto);
        }
        return result;
    }

    @Override
    public ErrorResponse authenticateUser(String email, String password) {
        ErrorResponse response = null;

        AppUser user = userRepo.findByEmail(email);
        if (user == null) {
            response = new ErrorResponse();
            response.setErrorMessage("Email address is not registered.");
            return response;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response = new ErrorResponse();
            response.setErrorMessage("Password does not match for this email.");
        }

        return response;
    }

    @Override
    public AppUser updateUser(Long id, JsonPatch patch) throws UserNotFoundByIdException, JsonPatchException, JsonProcessingException {
        AppUser user = userRepo.findById(id).orElseThrow(UserNotFoundByIdException::new);
        AppUser patchedUser = applyPatchToCustomer(patch, user);
        userRepo.save(patchedUser);
        return patchedUser;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("No user found with this email: " + email);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new User(user.getEmail(), user.getPassword(), authorities);
    }

    private AppUser saveUserByRole(AppUser user, String roleName) throws EmailAlreadyRegisteredException {
        AppUser existingUser = userRepo.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new EmailAlreadyRegisteredException();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        AppRole roleUser = roleRepo.findByName(roleName);
        user.getRoles().add(roleUser);
        return userRepo.save(user);
    }

    private AppUser applyPatchToCustomer(JsonPatch patch, AppUser targetUser) throws JsonPatchException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patched, AppUser.class);
    }

    private AppRole convertStringToAppRole(String roleType) {
        if (roleType.equals("admin")) {
            return this.roleRepo.findByName(ROLE_ADMIN);
        }

        if (roleType.equals("user")) {
            return this.roleRepo.findByName(ROLE_USER);
        }

        return null;
    }

    private AppUser checkUserExists() throws UserNotFoundByEmailException {
        String email = authenticationFacade.getAuthentication().getName();
        AppUser user = userRepo.findByEmail(email);

        if (user == null) {
            throw new UserNotFoundByEmailException();
        }

        return user;
    }
}
