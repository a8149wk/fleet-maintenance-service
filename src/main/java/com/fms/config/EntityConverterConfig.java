package com.fms.config;

import com.fms.entity.Client;
import com.fms.entity.SparePart;
import com.fms.entity.Workshop;
import com.fms.repository.ClientRepository;
import com.fms.repository.SparePartRepository;
import com.fms.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class EntityConverterConfig implements WebMvcConfigurer {

    private final ClientRepository clientRepository;
    private final WorkshopRepository workshopRepository;
    private final SparePartRepository sparePartRepository;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, Client>() {
            @Override
            public Client convert(String source) {
                if (source == null || source.isBlank()) return null;
                return clientRepository.findById(Long.valueOf(source)).orElse(null);
            }
        });
        registry.addConverter(new Converter<String, Workshop>() {
            @Override
            public Workshop convert(String source) {
                if (source == null || source.isBlank()) return null;
                return workshopRepository.findById(Long.valueOf(source)).orElse(null);
            }
        });
        registry.addConverter(new Converter<String, SparePart>() {
            @Override
            public SparePart convert(String source) {
                if (source == null || source.isBlank()) return null;
                return sparePartRepository.findById(Long.valueOf(source)).orElse(null);
            }
        });
    }
}
