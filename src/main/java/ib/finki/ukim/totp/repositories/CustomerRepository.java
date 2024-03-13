package ib.finki.ukim.totp.repositories;

import ib.finki.ukim.totp.models.Customer;
import ib.finki.ukim.totp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Customer findById(Long id);
    Customer findByTransactionNumber(String transactionNumber);
    Customer findByEmail(String email);
    Customer findByConfirmationToken(String token);
}
