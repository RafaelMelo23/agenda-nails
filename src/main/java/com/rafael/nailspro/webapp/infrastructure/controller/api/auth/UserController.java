package com.rafael.nailspro.webapp.infrastructure.controller.api.auth;

import com.rafael.nailspro.webapp.application.user.PasswordResetUseCase;
import com.rafael.nailspro.webapp.application.user.UserProfileManagementUseCase;
import com.rafael.nailspro.webapp.domain.model.UserPrincipal;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.ChangeEmailRequestDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.ChangePhoneRequestDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.ResetPasswordDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserProfileManagementUseCase userService;
    private final PasswordResetUseCase passwordResetUseCase;

    @PatchMapping("/email")
    public ResponseEntity<Void> updateEmail(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                            @Valid @RequestBody ChangeEmailRequestDTO dto) {

        userService.updateEmail(userPrincipal.getUserId(), dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/phone")
    public ResponseEntity<Void> updatePhone(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                            @Valid @RequestBody ChangePhoneRequestDTO dto) {

        userService.updatePhone(userPrincipal.getUserId(), dto);
        return ResponseEntity.noContent().build();
    }

    //todo: test
    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgotPassword(@RequestParam @NotBlank(message = "O e-mail é obrigatório")
                                               @Email(message = "O e-mail deve ser válido")
                                               String userEmail) {

        passwordResetUseCase.forgotPasswordRequest(userEmail);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody ResetPasswordDTO dto) {

        passwordResetUseCase.resetPassword(dto);
        return ResponseEntity.noContent().build();
    }
}