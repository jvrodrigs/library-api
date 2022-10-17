package com.book.libraryapi.Service;

import java.util.List;

public interface EmailService {
    void sendMails(String msg, List<String> mailsList);
}
