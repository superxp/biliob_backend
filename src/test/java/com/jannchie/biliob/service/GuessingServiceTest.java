package com.jannchie.biliob.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class GuessingServiceTest {

    @Autowired
    GuessingService guessingService;


    @Test
    public void printGuessingResult() {
        guessingService.printGuessingResult("5ec2819bbc9a844f180ade97");
    }
}