package com.rafael.agendanails.webapp.shared.tenant;

import com.rafael.agendanails.webapp.domain.model.Client;
import com.rafael.agendanails.webapp.domain.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IgnoreFilterTestService {
    @Autowired
    private ClientRepository clientRepository;

    @IgnoreTenantFilter
    public List<Client> findAllIgnored() {
        return clientRepository.findAll();
    }

    @IgnoreTenantFilter
    public Optional<Client> findByEmailIgnored(String email) {
        return clientRepository.findByEmailIgnoreCase(email);
    }

    public Optional<Client> findByEmailNotIgnored(String email) {
        return clientRepository.findByEmailIgnoreCase(email);
    }
}
