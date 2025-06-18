package com.example.reservationsystem.exception

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외 (HTTP 404 Not Found)
 */
class NotFoundException(message: String) : RuntimeException(message)

/**
 * 요청이 현재 서버의 상태와 충돌할 때 발생하는 예외 (HTTP 409 Conflict)
 * 예: 이미 예약된 좌석을 다시 예약하려는 경우
 */
class ConflictException(message: String) : RuntimeException(message)

/**
 * 인증은 되었으나 특정 리소스에 대한 접근 권한이 없을 때 발생하는 예외 (HTTP 403 Forbidden)
 */
class ForbiddenException(message: String) : RuntimeException(message)

/**
 * 클라이언트의 잘못된 요청으로 인해 작업을 수행할 수 없을 때 발생하는 예외 (HTTP 400 Bad Request)
 */
class BadRequestException(message: String) : RuntimeException(message)

/**
 * 주문 생성과 같은 핵심 비즈니스 로직 실패 시 발생하는 예외 (HTTP 500 Internal Server Error)
 */
class OrderProcessingException(message: String) : RuntimeException(message)
