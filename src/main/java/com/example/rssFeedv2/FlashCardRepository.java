package com.example.rssFeedv2;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashCardRepository extends CrudRepository<FlashCard, String> {
}