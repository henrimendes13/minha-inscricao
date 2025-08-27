package br.com.eventsports.minha_inscricao.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// @EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList("eventos", "equipes", "usuarios", "categorias", "inscricoes",
                "organizadores", "atletas", "workouts", "anexos", "timelines", "leaderboard"));
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }
}
