package com.rafael.nailspro.webapp.application.user;

import com.rafael.nailspro.webapp.domain.model.Client;
import com.rafael.nailspro.webapp.domain.model.User;
import com.rafael.nailspro.webapp.domain.repository.ClientRepository;
import com.rafael.nailspro.webapp.domain.repository.UserRepository;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.ChangeEmailRequestDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.ChangePhoneRequestDTO;
import com.rafael.nailspro.webapp.infrastructure.dto.auth.ResetPasswordDTO;
import com.rafael.nailspro.webapp.infrastructure.email.EmailService;
import com.rafael.nailspro.webapp.infrastructure.email.template.EmailTemplateBuilder;
import com.rafael.nailspro.webapp.infrastructure.exception.BusinessException;
import com.rafael.nailspro.webapp.infrastructure.security.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final EmailTemplateBuilder emailTemplateBuilder;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TokenService tokenService;
    @Value("${domain.url}")
    private String domainUrl;

    @Transactional
    public void updateEmail(Long userId, ChangeEmailRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Senha incorreta. Não foi possível alterar o e-mail.");
        }

        if (userRepository.existsByEmail(request.newEmail())) {
            throw new RuntimeException("Este e-mail já está em uso por outra conta.");
        }

        user.setEmail(request.newEmail());
        userRepository.save(user);
    }

    @Transactional
    public void updatePhone(Long clientId, ChangePhoneRequestDTO request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.password(), client.getPassword())) {
            throw new BusinessException("Senha incorreta. Não foi possível alterar o telefone.");
        }

        String cleanPhone = request.newPhone().replaceAll("\\D", "");

        if (clientRepository.existsByPhoneNumber(cleanPhone)) {
            throw new BusinessException("Este telefone já está vinculado a outra conta.");
        }

        client.setPhoneNumber(cleanPhone);
        clientRepository.save(client);
    }

    public void forgotPasswordRequest(String userEmail) {
        Long userId = userRepository.getUser_IdByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        String resetToken = tokenService.generateResetPasswordToken(userId);
        String passwordResetLink = domainUrl + "/redefinir-senha?resetToken=" + resetToken;

        String forgotPasswordEmail = emailTemplateBuilder.buildForgotPasswordEmail(userEmail, passwordResetLink);
        emailService.sendEmail(userEmail, "Redefinição de senha", forgotPasswordEmail);
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        tokenService.validateResetPasswordToken(resetPasswordDTO);

        userRepository.updatePassword(
                resetPasswordDTO.userEmail(),
                passwordEncoder.encode(resetPasswordDTO.newPassword()));
    }
}
