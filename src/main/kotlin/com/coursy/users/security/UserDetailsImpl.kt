package com.coursy.users.security

import com.coursy.users.model.User
import com.coursy.users.types.Email
import com.coursy.users.types.Login
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImp(
    val id: Long,
    val email: Email,
    private val authorities: MutableCollection<SimpleGrantedAuthority>,
    private val password: String,
    private val login: Login,
    private val enabled: Boolean,
    private val accountNonLocked: Boolean
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword() = password

    override fun getUsername() = email.value

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = accountNonLocked

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = enabled

}

fun User.toUserDetails(): UserDetailsImp {
    val authorities = mutableSetOf(SimpleGrantedAuthority(this.role.name.name))

    return UserDetailsImp(
        id = this.id,
        email = this.email,
        password = password,
        login = this.login,
        authorities = authorities,
        enabled = this.enabled,
        accountNonLocked = this.accountNonLocked
    )
}
