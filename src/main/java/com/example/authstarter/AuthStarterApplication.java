package com.example.authstarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 𝐍𝐎𝐓𝐄 :
 * <p>
 * All short-lived state in this starter lives in memory (Caffeine): email-verification, password-reset
 * and account-deletion codes, passkey challenges, rate-limit buckets and the user cache. That is fine for
 * a single instance, but it is lost on restart and not shared between instances.
 * </p>
 *
 * <p>
 * For horizontally scaled production environments, move this state to a distributed store such as Redis
 * (and Bucket4j's Redis integration for rate limits) so every instance sees the same data.
 * </p>
 *
 * @author 𝐏𝐄𝐓𝐄𝐑 𝐍𝐖𝐀𝐎𝐆𝐔
 */

@SpringBootApplication
public class AuthStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthStarterApplication.class, args);
    }

}
