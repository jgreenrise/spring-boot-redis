package com.example.rssFeedv2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private FlashCardRepository flashCardRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if flash cards already exist
        long count = flashCardRepository.count();
        if (count == 0) {
            // Create sample flash cards
            List<FlashCard> sampleCards = Arrays.asList(
                new FlashCard("What is the capital of France?", "Paris", "Geography", "EASY"),
                new FlashCard("What is 2 + 2?", "4", "Mathematics", "EASY"),
                new FlashCard("Who wrote 'Romeo and Juliet'?", "William Shakespeare", "Literature", "MEDIUM"),
                new FlashCard("What is the chemical symbol for gold?", "Au", "Chemistry", "MEDIUM"),
                new FlashCard("In which year did World War II end?", "1945", "History", "MEDIUM"),
                new FlashCard("What is the speed of light in vacuum?", "299,792,458 m/s", "Physics", "HARD"),
                new FlashCard("What is the largest planet in our solar system?", "Jupiter", "Astronomy", "EASY"),
                new FlashCard("Who developed the theory of relativity?", "Albert Einstein", "Physics", "MEDIUM"),
                new FlashCard("What is the smallest unit of matter?", "Atom", "Chemistry", "EASY"),
                new FlashCard("What programming language is known for 'Write Once, Run Anywhere'?", "Java", "Programming", "MEDIUM")
            );

            flashCardRepository.saveAll(sampleCards);
            System.out.println("Sample flash cards have been created!");
        } else {
            System.out.println("Flash cards already exist in the database. Skipping initialization.");
        }
    }
}