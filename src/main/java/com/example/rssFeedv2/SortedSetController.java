package com.example.rssFeedv2;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sortedset")
public class SortedSetController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/add/{key}")
    public void addValueToSortedSet(@PathVariable String key, @RequestParam String user, @RequestParam double score) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(key, user, score);
    }

    @PutMapping("/increment/{key}")
    public Double incrementValueScore(@PathVariable String key, @RequestParam String user, @RequestParam double score) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.incrementScore(key, user, score);
    }

    @GetMapping("/range/{key}")
    public Set<String> getRange(@PathVariable String key, @RequestParam long start, @RequestParam long end) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.range(key, start, end);
    }

    @GetMapping("/reverse-range/{key}")
    public Set<String> getReverseRange(@PathVariable String key, @RequestParam long start, @RequestParam long end) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.reverseRange(key, start, end);
    }

    @GetMapping("/rank/{key}")
    public Long getRank(@PathVariable String key, @RequestParam String user) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.rank(key, user);
    }
}

