package com.theplus.cyberguard.alchemist.reasoning;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlchemistClaudeConfig {

    @Bean
    @ConditionalOnExpression("'${alchemist.reasoning.api-key:}' != ''")
    public AnthropicClient alchemistAnthropicClient(@Value("${alchemist.reasoning.api-key}") String apiKey) {
        return AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }
}
