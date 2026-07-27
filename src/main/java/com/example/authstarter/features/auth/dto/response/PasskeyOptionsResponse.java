package com.example.authstarter.features.auth.dto.response;

import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;

import java.util.List;

public record PasskeyOptionsResponse(
        RpDto rp,
        UserDto user,
        String challenge,
        List<PubKeyCredParamDto> pubKeyCredParams,
        Long timeout,
        List<CredentialDescriptorDto> excludeCredentials,
        AuthenticatorSelectionDto authenticatorSelection,
        String attestation
) {
    private record RpDto(String id, String name) {}

    private record UserDto(String id, String name, String displayName) {}

    private record PubKeyCredParamDto(String type, long alg) {}

    private record CredentialDescriptorDto(String id, String type, List<String> transports) {}

    private record AuthenticatorSelectionDto(
            String authenticatorAttachment,
            String residentKey,
            boolean requireResidentKey,
            String userVerification
    ) {}

    public static PasskeyOptionsResponse toResponse(PublicKeyCredentialCreationOptions options){
        assert options.getTimeout() != null;
        assert options.getAuthenticatorSelection().getResidentKey() != null;
        assert options.getAuthenticatorSelection().getUserVerification() != null;
        assert options.getAttestation() != null;
        return new PasskeyOptionsResponse(
                new PasskeyOptionsResponse.RpDto(
                        options.getRp().getId(),
                        options.getRp().getName()
                ),

                new UserDto(
                        options.getUser().getId().toBase64UrlString(),
                        options.getUser().getName(),
                        options.getUser().getDisplayName()
                ),

                options.getChallenge().toBase64UrlString(),
                options.getPubKeyCredParams().stream()
                        .map(p -> new PasskeyOptionsResponse.PubKeyCredParamDto(
                                p.getType().getValue(), p.getAlg().getValue()))
                        .toList(),
                options.getTimeout().toMillis(),
                options.getExcludeCredentials().stream()
                        .map(c -> {
                            assert c.getId() != null;
                            assert c.getTransports() != null;
                            return new CredentialDescriptorDto(
                                    c.getId().toBase64UrlString(),
                                    c.getType().getValue(),
                                    c.getTransports().stream().map(AuthenticatorTransport::getValue).toList());
                        })
                        .toList(),
                new PasskeyOptionsResponse.AuthenticatorSelectionDto(
                        options.getAuthenticatorSelection().getAuthenticatorAttachment() != null
                                ? options.getAuthenticatorSelection().getAuthenticatorAttachment().getValue() : null,
                        options.getAuthenticatorSelection().getResidentKey().getValue(),
                        options.getAuthenticatorSelection().getResidentKey().getValue().equals("required"),
                        options.getAuthenticatorSelection().getUserVerification().getValue()
                ),
                options.getAttestation().getValue()
        );
    }
}
