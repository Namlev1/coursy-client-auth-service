import com.auth0.jwt.interfaces.DecodedJWT
import com.coursy.users.security.AuthenticatedUser
import com.coursy.users.types.Email
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.util.*


fun PreAuthenticatedAuthenticationToken.readToken(): Pair<Email, UUID> {
    val jwt = this.credentials as DecodedJWT
    val id = UUID.fromString(jwt.getClaim("id").asString())
    val email = this.principal as Email
    return Pair(email, id)
}

fun PreAuthenticatedAuthenticationToken.getAuthenticatedUser(): AuthenticatedUser? {
    return this.principal as? AuthenticatedUser
}

fun PreAuthenticatedAuthenticationToken.getTenantId(): UUID? {
    return this.getAuthenticatedUser()?.tenantId
}

fun PreAuthenticatedAuthenticationToken.getEmail(): Email? {
    return this.getAuthenticatedUser()?.email
}