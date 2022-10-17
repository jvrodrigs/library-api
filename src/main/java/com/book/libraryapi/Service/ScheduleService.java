package com.book.libraryapi.Service;

import com.book.libraryapi.Model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final String CRON_SCHEDULED = "0 0 0 1/1 * ?";

    private final LoanService loanService;
    private final EmailService emailService;

    @Value("${application.mail.lateloans.message}")
    private String msg;

    @Scheduled(cron = CRON_SCHEDULED)
    public void sendMailToLateLoans(){
        List<Loan> allLateLoans = loanService.getAllLateLoans();

        List<String> mailsList = allLateLoans.stream()
                .map(loan -> loan.getCustomerEmail())
                .collect(Collectors.toList());
        emailService.sendMails(msg, mailsList);
    }
}
