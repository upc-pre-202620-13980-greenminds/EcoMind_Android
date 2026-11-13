package pe.greenminds.ecomind.shared.domain.model

class SessionRequiredException : IllegalStateException("A current session is required")
