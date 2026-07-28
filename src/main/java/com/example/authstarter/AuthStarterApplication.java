package com.example.authstarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 𝐍𝐎𝐓𝐄 :
 * <p>
 * OTP service responsible for generating, validating, and managing OTP requests.
 * </p>
 *
 * <p>
 * This implementation uses in-memory caching and Bucket4j-based rate limiting,
 * making it suitable for single-instance deployments.
 * </p>
 *
 * <p>
 * For horizontally scaled production environments, a distributed storage
 * mechanism such as Redis should be used to maintain consistent OTP state
 * and rate limits across application instances.
 * </p>
 *
 * @author 𝐏𝐄𝐓𝐄𝐑 𝐍𝐖𝐀𝐎𝐆𝐔
 */

@SpringBootApplication
@EnableJpaAuditing
public class AuthStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthStarterApplication.class, args);
    }

}
