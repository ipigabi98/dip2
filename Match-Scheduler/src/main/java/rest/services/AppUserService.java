package rest.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import rest.data.dto.AdminUserDto;
import rest.data.dto.UserDetailDto;
import rest.data.entities.AppRole;
import rest.data.entities.AppUser;
import rest.data.exceptions.*;
import rest.data.requests.ChangePasswordRequest;
import rest.data.responses.ErrorResponse;
import rest.data.responses.ProcessResponse;

import java.util.List;

public interface AppUserService {
    AppUser saveUser(AppUser user) throws EmailAlreadyRegisteredException;
    AppUser saveAdmin(AppUser user) throws EmailAlreadyRegisteredException;
    void deleteUser(Long userId) throws UserNotFoundByIdException;
    AppRole saveRole(AppRole role) throws RoleAlreadyExistsException;
    void assignRoleToUser(Long userId, String roleName) throws UserNotFoundByIdException;
    void removeRoleFromUser(Long userId, String roleName) throws UserNotFoundByIdException, RoleDoesNotExistException;
    AppUser findByEmail(String email);
    ProcessResponse changePassword(ChangePasswordRequest request) throws UserNotFoundByEmailException, PasswordsDoNotMatchException;
    AppUser findById(Long id) throws UserNotFoundByIdException;
    UserDetailDto getUserEmail() throws UserNotFoundByEmailException;
    List<AdminUserDto> findAll();
    ErrorResponse authenticateUser(String email, String password);
    AppUser updateUser(Long id, JsonPatch patch) throws UserNotFoundByIdException, JsonPatchException, JsonProcessingException;
}
