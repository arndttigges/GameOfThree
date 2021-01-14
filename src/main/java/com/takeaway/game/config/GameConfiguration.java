package com.takeaway.game.config;

import com.takeaway.game.rule.DivideByThreeRule;
import com.takeaway.game.rule.Rule;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class GameConfiguration {

    @Bean
    public Rule divideByThreeRule() {
        return new DivideByThreeRule();
    }

}
