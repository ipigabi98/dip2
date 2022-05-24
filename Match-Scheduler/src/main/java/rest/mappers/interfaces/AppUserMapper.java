package rest.mappers.interfaces;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import rest.data.dto.AdminUserDto;
import rest.data.entities.AppRole;
import rest.data.entities.AppUser;

import java.util.Collection;

@Mapper(componentModel="spring")
public interface AppUserMapper {

    @Mappings({
            @Mapping(source = "user.roles", target = "hasUserRole", qualifiedByName = "hasUserRoleMethod"),
            @Mapping(source = "user.roles", target = "hasAdminRole", qualifiedByName = "hasAdminRoleMethod")
    })
    AdminUserDto entityToDto(AppUser user);

    @Named("hasUserRoleMethod")
    static Boolean hasUserRoleMethod(Collection<AppRole> roles) {
        for (AppRole role : roles) {
            if (role.getName().equals("ROLE_USER")) {
                return true;
            }
        }
        return false;
    }

    @Named("hasAdminRoleMethod")
    static Boolean hasAdminRoleMethod(Collection<AppRole> roles) {
        for (AppRole role : roles) {
            if (role.getName().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }
}
