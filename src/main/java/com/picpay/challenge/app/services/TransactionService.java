package com.picpay.challenge.app.services;

import com.picpay.challenge.app.domain.transaction.Transaction;
import com.picpay.challenge.app.domain.user.User;
import com.picpay.challenge.app.dtos.TransactionDTO;
import com.picpay.challenge.app.repository.TransactionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionalRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationService notificationService;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {

        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validateTransaction(sender, transaction.value());

        boolean isAuthorized = this.authorizeTransaction(sender, transaction.value());
        if (!isAuthorized) {
            throw new Exception("Unauthorized transaction");
        }

        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setTimestamp(LocalDate.from(LocalDateTime.now()));

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));

        this.repository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);

        this.notificationService.sendNotification(sender, "Transaction completed successfully!");
        this.notificationService.sendNotification(receiver, "Transaction received successfully!");

        return newTransaction;
    }

    private final BigDecimal transactionLimit = new BigDecimal("1000.00"); // Exemplo: R$ 1000,00

    public boolean authorizeTransaction(User sender, BigDecimal value) {
        if (value.compareTo(transactionLimit) <= 0) {
            // Transação abaixo do limite, autorizar automaticamente
            return true;
        } else {
            // Transação acima do limite, verificar com o servidor externo
            ResponseEntity<Map> authorizationResponse = restTemplate.getForEntity("https://util.devi.tools/api/v1/notify", Map.class);

            if (authorizationResponse.getStatusCode() == HttpStatus.OK) {
                String message = (String) authorizationResponse.getBody().get("message");
                return "Route 'GET:/api/v1/notify' not found".equalsIgnoreCase(message);
            } else return false;
        }
    }

}
