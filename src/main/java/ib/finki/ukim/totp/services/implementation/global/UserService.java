package ib.finki.ukim.totp.services.implementation.global;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.repositories.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final CustomerRepository customerRepository;

    public UserService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerById(Long id){
        return customerRepository.findById(id);
    }
}
