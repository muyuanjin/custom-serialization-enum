package com.muyuanjin;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import com.muyuanjin.annotating.EnumSerializeProxy;
import com.muyuanjin.map.CustomSerializationEnumJsonDeserializer;
import com.muyuanjin.map.CustomSerializationEnumJsonSerializer;
import com.muyuanjin.map.CustomSerializationEnumTypeHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author muyuanjin
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class TestConfig {
    private final List<Class<EnumSerialize<?>>> enumSerializes;

    public TestConfig(@Value("${custom-serialization-enum.path:'com'}") String path) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        enumSerializes = getEnumSerializes(provider, path);
        //noinspection unchecked,rawtypes
        enumSerializes.addAll((List) getAnnotatedEnums(provider, path));
    }

    @Autowired
    @SuppressWarnings({"unchecked", "rawtypes"})
    void registryConverter(ConverterRegistry converterRegistry) {
        for (Class<EnumSerialize<?>> enumSerialize : enumSerializes) {
            CustomSerializationEnum annotation = AnnotationUtils.findAnnotation(enumSerialize, CustomSerializationEnum.class);
            CustomSerializationEnum.Type type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.requestParam();
            converterRegistry.addConverter(String.class, (Class) enumSerialize, t -> type.getDeserializeObj((Class) enumSerialize, t));
        }
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.modules(new SimpleModule() {
            {
                for (Class<EnumSerialize<?>> enumSerialize : enumSerializes) {
                    addDeserializer(enumSerialize, new CustomSerializationEnumJsonDeserializer(enumSerialize));
                    addSerializer(enumSerialize, new CustomSerializationEnumJsonSerializer(enumSerialize));
                }
            }
        });
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return t -> {
            for (Class<EnumSerialize<?>> enumSerialize : enumSerializes) {
                t.getTypeHandlerRegistry().register(enumSerialize, new CustomSerializationEnumTypeHandler(enumSerialize));
            }
        };
    }

    /**
     * 通过父类class和类路径获取该路径下父类的所有子类列表
     *
     * @return 所有该类子类或实现类的列表
     */
    @SneakyThrows(ClassNotFoundException.class)
    private static List<Class<EnumSerialize<?>>> getEnumSerializes(ClassPathScanningCandidateComponentProvider provider, String path) {
        provider.resetFilters(false);
        provider.addIncludeFilter(new AssignableTypeFilter(EnumSerialize.class));
        final Set<BeanDefinition> components = provider.findCandidateComponents(path);
        final List<Class<EnumSerialize<?>>> subClasses = new ArrayList<>();
        for (final BeanDefinition component : components) {
            @SuppressWarnings("unchecked") final Class<EnumSerialize<?>> cls = (Class<EnumSerialize<?>>) Class.forName(component.getBeanClassName());
            if (cls.equals(EnumSerializeProxy.class)) {
                continue;
            }
            if (cls.isEnum()) {
                subClasses.add(cls);
            } else {
                throw new UnsupportedOperationException("Class:" + cls.getCanonicalName() + "is not enum! " +
                        "The class that implements the \"EnumSerialize\" must be an enumeration class.");
            }
        }
        return subClasses;
    }

    @SneakyThrows(ClassNotFoundException.class)
    private static List<Class<Enum<?>>> getAnnotatedEnums(ClassPathScanningCandidateComponentProvider provider, String path) {
        provider.resetFilters(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(CustomSerializationEnum.class));
        provider.addExcludeFilter(new AssignableTypeFilter(EnumSerialize.class));
        final Set<BeanDefinition> components = provider.findCandidateComponents(path);
        final List<Class<Enum<?>>> enumClasses = new ArrayList<>();
        for (final BeanDefinition component : components) {
            @SuppressWarnings("unchecked") final Class<Enum<?>> cls = (Class<Enum<?>>) Class.forName(component.getBeanClassName());
            if (cls.isEnum()) {
                enumClasses.add(cls);
            } else {
                throw new UnsupportedOperationException("Class:" + cls.getCanonicalName() + "is not enum! " +
                        "The class annotated by \"CustomSerializationEnum\" must be an enumeration class.");
            }
        }
        return enumClasses;
    }

}
