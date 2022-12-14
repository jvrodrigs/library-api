package com.book.libraryapi.Service.Impl;

import com.book.libraryapi.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${application.mail.default-remetent}")
    private String remetent;
    private final JavaMailSender jvMailSender;

    @Override
    public void sendMails(String msg, List<String> mailsList) {
        String[] mails = mailsList.toArray(new String[mailsList.size()]);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(remetent);
        mailMessage.setSubject("Livro com empréstimo atrasado!");
        mailMessage.setText(msg);
        mailMessage.setTo(mails);

        jvMailSender.send(mailMessage);
    }
}
