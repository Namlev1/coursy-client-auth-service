package com.coursy.clientauthservice.service

//@Service
//@Transactional
//class RefreshTokenService(
//    private val refreshTokenRepository: RefreshTokenRepository,
//    private val userRepository: UserRepository,
//    @Value("\${jwt.refresh-token-expiration}")
//    private val refreshTokenDurationMs: Long
//) {
//    fun createRefreshToken(userId: Long): Either<UserFailure, RefreshToken> {
//        val user = userRepository.findById(userId).getOrNull()
//            ?: return UserFailure.IdNotExists.left()
//
//        refreshTokenRepository.deleteByUser(user)
//        refreshTokenRepository.flush()
//
//        val refreshToken = RefreshToken(
//            user = user,
//            token = UUID.randomUUID().toString(),
//            expiryDate = Instant.now().plusMillis(refreshTokenDurationMs)
//        )
//
//        return refreshTokenRepository.save(refreshToken).right()
//    }
//
//    fun findByToken(token: String) =
//        refreshTokenRepository.findByToken(token)
//            ?.right() ?: RefreshTokenFailure.NotFound.left()
//
//    fun verifyExpiration(token: RefreshToken): Either<RefreshTokenFailure, RefreshToken> {
//        return if (token.expiryDate.isBefore(Instant.now())) {
//            refreshTokenRepository.delete(token)
//            RefreshTokenFailure.Expired.left()
//        } else {
//            token.right()
//        }
//    }
//}
