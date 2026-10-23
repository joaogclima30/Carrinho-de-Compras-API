package io.github.joaogclima30.carrinhoAPI.security.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LojaConfig {

    @Bean
    public ModelMapper modelMapper (){
        return new ModelMapper();
    }
}
