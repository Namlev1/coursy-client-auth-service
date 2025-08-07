import arrow.core.getOrElse
import com.coursy.users.model.Role
import com.coursy.users.model.toRole
import com.coursy.users.security.AuthenticatedUser
import com.coursy.users.types.Email
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.util.*


fun PreAuthenticatedAuthenticationToken.getAuthenticatedUser(): AuthenticatedUser? {
    return this.principal as? AuthenticatedUser
}

fun PreAuthenticatedAuthenticationToken.getPlatformId(): UUID? {
    return this.getAuthenticatedUser()?.platformId
}

fun PreAuthenticatedAuthenticationToken.getEmail(): Email {
    return this.getAuthenticatedUser()?.email
        ?: throw IllegalStateException("Authentication token missing email")
}

fun PreAuthenticatedAuthenticationToken.getId(): UUID {
    return this.getAuthenticatedUser()?.id
        ?: throw IllegalStateException("Authentication token missing user ID")
}

fun PreAuthenticatedAuthenticationToken.getRole(): Role {
    return this
        .authorities
        .first()
        .toRole()
        .getOrElse {
            throw IllegalStateException("Authentication token missing role")
        }
}