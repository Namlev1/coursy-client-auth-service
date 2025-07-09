import com.auth0.jwt.interfaces.DecodedJWT
import com.coursy.users.types.Email
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import java.util.*


fun PreAuthenticatedAuthenticationToken.readToken(): Pair<Email, UUID> {
    val jwt = this.credentials as DecodedJWT
    val id = UUID.fromString(jwt.getClaim("id").asString())
    val email = this.principal as Email
    return Pair(email, id)
}
