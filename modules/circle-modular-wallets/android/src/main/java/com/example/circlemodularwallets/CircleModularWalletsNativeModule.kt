package com.example.circlemodularwallets

import com.circle.modularwallets.core.accounts.WebAuthnAccount
import com.circle.modularwallets.core.accounts.WebAuthnCredential
import com.circle.modularwallets.core.accounts.toWebAuthnAccount
import com.circle.modularwallets.core.accounts.toWebAuthnCredential
import com.circle.modularwallets.core.models.RegistrationCredential
import com.circle.modularwallets.core.models.WebAuthnMode
import com.circle.modularwallets.core.transports.http.toPasskeyTransport
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.functions.Coroutine
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class CircleModularWalletsNativeModule : Module() {
  private var account: WebAuthnAccount? = null

  override fun definition() = ModuleDefinition {
    Name("CircleModularWalletsNative")

    AsyncFunction("register") Coroutine { clientKey: String, clientUrl: String, userName: String ->
      val activity = appContext.throwingActivity
      val transport = toPasskeyTransport(activity, clientKey, clientUrl)
      val credential = toWebAuthnCredential(
        activity,
        transport,
        userName,
        WebAuthnMode.Register
      )
      account = toWebAuthnAccount(credential)
      serializeCredential(credential, includeRegistration = true)
    }

    AsyncFunction("login") Coroutine { clientKey: String, clientUrl: String ->
      val activity = appContext.throwingActivity
      val transport = toPasskeyTransport(activity, clientKey, clientUrl)
      val credential = toWebAuthnCredential(
        activity,
        transport,
        mode = WebAuthnMode.Login
      )
      account = toWebAuthnAccount(credential)
      serializeCredential(credential, includeRegistration = false)
    }

    AsyncFunction("sign") Coroutine { messageHash: String ->
      val activity = appContext.throwingActivity
      val hydratedAccount = account ?: throw CircleNativeCredentialNotHydratedException()
      val result = hydratedAccount.sign(activity, messageHash)
      val raw = result.raw
      mapOf(
        "signature" to result.signature,
        "webauthn" to mapOf(
          "authenticatorData" to result.webAuthn.authenticatorData,
          "clientDataJSON" to result.webAuthn.clientDataJSON,
          "challengeIndex" to result.webAuthn.challengeIndex,
          "typeIndex" to result.webAuthn.typeIndex,
          "userVerificationRequired" to result.webAuthn.userVerificationRequired
        ),
        "raw" to mapOf(
          "id" to raw.id,
          "rawId" to raw.rawId,
          "type" to raw.type,
          "authenticatorAttachment" to raw.authenticatorAttachment,
          "response" to mapOf(
            "clientDataJSON" to raw.response.clientDataJSON,
            "authenticatorData" to raw.response.authenticatorData,
            "signature" to raw.response.signature,
            "userHandle" to raw.response.userHandle
          )
        )
      )
    }
  }

  private fun serializeCredential(
    credential: WebAuthnCredential,
    includeRegistration: Boolean
  ): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>(
      "id" to credential.id,
      "publicKey" to credential.publicKey,
      "rpId" to credential.rpId
    )

    if (includeRegistration) {
      val registration = credential.raw as? RegistrationCredential
      if (registration != null) {
        result["registration"] = mapOf(
          "id" to registration.id,
          "rawId" to registration.rawId,
          "type" to registration.type,
          "authenticatorAttachment" to registration.authenticatorAttachment,
          "response" to mapOf(
            "clientDataJSON" to registration.response.clientDataJSON,
            "attestationObject" to registration.response.attestationObject,
            "publicKey" to registration.response.publicKey,
            "transports" to registration.response.transports
          )
        )
      }
    }

    return result
  }
}

private class CircleNativeCredentialNotHydratedException : CodedException(
  "Circle native passkey account is not hydrated. Register or reconnect the credential before signing."
)
