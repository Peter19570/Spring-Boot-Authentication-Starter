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

    public static PasskeyOptionsResponse toResponse(PublicKeyCredentialCreationOptions options) {
        var selection = options.getAuthenticatorSelection();
        var attachment = selection != null ? selection.getAuthenticatorAttachment() : null;
        var residentKey = selection != null ? selection.getResidentKey() : null;
        var userVerification = selection != null ? selection.getUserVerification() : null;

        List<CredentialDescriptorDto> excluded = options.getExcludeCredentials() == null
                ? List.of()
                : options.getExcludeCredentials().stream()
                        .map(c -> new CredentialDescriptorDto(
                                c.getId().toBase64UrlString(),
                                c.getType().getValue(),
                                c.getTransports() == null
                                        ? List.of()
                                        : c.getTransports().stream().map(AuthenticatorTransport::getValue).toList()))
                        .toList();

        return new PasskeyOptionsResponse(
                new RpDto(options.getRp().getId(), options.getRp().getName()),

                new UserDto(
                        options.getUser().getId().toBase64UrlString(),
                        options.getUser().getName(),
                        options.getUser().getDisplayName()
                ),

                options.getChallenge().toBase64UrlString(),

                options.getPubKeyCredParams().stream()
                        .map(p -> new PubKeyCredParamDto(p.getType().getValue(), p.getAlg().getValue()))
                        .toList(),

                options.getTimeout() != null ? options.getTimeout().toMillis() : null,

                excluded,

                new AuthenticatorSelectionDto(
                        attachment != null ? attachment.getValue() : null,
                        residentKey != null ? residentKey.getValue() : null,
                        residentKey != null && "required".equals(residentKey.getValue()),
                        userVerification != null ? userVerification.getValue() : null
                ),

                options.getAttestation() != null ? options.getAttestation().getValue() : null
        );
    }
}