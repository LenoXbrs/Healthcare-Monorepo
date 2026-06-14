package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.List;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.response.PacienteResponse;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * ObjectMapper para Redis sem DefaultTyping.EVERYTHING.
     * A api-usuarios usa EVERYTHING — vetor de ataque (polimorphic deserialization).
     * Aqui usamos apenas módulos necessários.
     */
    private ObjectMapper redisObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        ObjectMapper mapper = redisObjectMapper();

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();

        var pacSerializer = new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(mapper, PacienteResponse.class);
        var pacListSerializer = new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(mapper,
                mapper.getTypeFactory().constructCollectionType(List.class, PacienteResponse.class));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(Map.of(
                    // Dados de paciente por ID — muda raramente
                    "pacientes", base.entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(pacSerializer)),
                    // Lista por status — muda conforme triagens chegam
                    "pacientes-status", base.entryTtl(Duration.ofMinutes(1))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(pacListSerializer))
                ))
                .build();
    }
}
