package com.mvl.assignment.domain.model

import java.io.IOException

class NicknameTooLongException(val maxLength: Int) :
    IllegalArgumentException("Nickname must be at most $maxLength characters")

class EmptySlotException(val slot: Slot) :
    IllegalStateException("Slot $slot has no place")

class SelectionCompleteException :
    IllegalStateException("A and B are already set")

class IncompleteSelectionException :
    IllegalStateException("Both A and B must be set before booking")

class BookNotFoundException(val id: String) :
    NoSuchElementException("Book $id not found")

/** Location permission missing, location services off, or no fix available. */
class LocationUnavailableException(message: String = "Current location unavailable") :
    IllegalStateException(message)

// Remote failures are IOExceptions so OkHttp delivers them as call failures instead of crashing its threads.

/** A service answered with an error (HTTP error code, error payload or unreadable body). */
class RemoteServiceException(message: String) : IOException(message)

/** A required key is missing from local.properties. */
class MissingApiKeyException(val keyName: String) :
    IOException("$keyName is not configured in local.properties")
