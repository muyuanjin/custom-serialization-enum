package com.muyuanjin;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import com.muyuanjin.annotating.EnumSerializeAdapter;
import com.muyuanjin.map.CustomSerializationEnumJsonDeserializer;
import com.muyuanjin.map.CustomSerializationEnumJsonSerializer;
import com.muyuanjin.map.CustomSerializationEnumTypeHandler;
import javafx.util.Pair;
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
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.*;

/**
 * @author muyuanjin
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class TestConfig {
    private final Map<Class<Enum<?>>, Set<EnumSerialize<?>>> enumSerializes;

    /**
     * 在spring 配置文件中使用custom-serialization-enum.path即可配置扫描路径，没有配置就使用com.muyuanjin作为默认值
     * 如果需要作为基础组件在多个项目中使用，就不是这样配置了，但是原理是一样的
     */
    public TestConfig(@Value("${custom-serialization-enum.path:com.muyuanjin}") String path) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        enumSerializes = getEnumSerializes(provider, path);
        enumSerializes.putAll(getAnnotatedEnums(provider, path));
    }

    @Autowired
    @SuppressWarnings({"unchecked", "rawtypes"})
    void registryConverter(ConverterRegistry converterRegistry) {
        for (Map.Entry<Class<Enum<?>>, Set<EnumSerialize<?>>> classSetEntry : enumSerializes.entrySet()) {
            Class clazz = classSetEntry.getKey();
            CustomSerializationEnum annotation = EnumSerialize.getAnnotation(clazz);
            CustomSerializationEnum.Type type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.requestParam();
            converterRegistry.addConverter(String.class, clazz, t -> type.getDeserializeObj(clazz, t));
        }
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.modules(new SimpleModule() {
            {
                for (Map.Entry<Class<Enum<?>>, Set<EnumSerialize<?>>> classSetEntry : enumSerializes.entrySet()) {
                    Class clazz = classSetEntry.getKey();
                    addDeserializer(clazz, new CustomSerializationEnumJsonDeserializer(new Pair<>(classSetEntry.getKey(), classSetEntry.getValue())));
                    addSerializer(clazz, new CustomSerializationEnumJsonSerializer(new Pair<>(classSetEntry.getKey(), classSetEntry.getValue())));
                }
            }
        });
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return t -> {
            for (Map.Entry<Class<Enum<?>>, Set<EnumSerialize<?>>> classSetEntry : enumSerializes.entrySet()) {
                Class clazz = classSetEntry.getKey();
                t.getTypeHandlerRegistry().register(clazz, new CustomSerializationEnumTypeHandler(new Pair<>(classSetEntry.getKey(), classSetEntry.getValue())));
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(ClassNotFoundException.class)
    private static Map<Class<Enum<?>>, Set<EnumSerialize<?>>> getEnumSerializes(ClassPathScanningCandidateComponentProvider provider, String path) {
        provider.resetFilters(false);
        provider.addIncludeFilter(new AssignableTypeFilter(EnumSerialize.class));
        final Set<BeanDefinition> components = provider.findCandidateComponents(path);
        final Map<Class<Enum<?>>, Set<EnumSerialize<?>>> enumSerializes = new HashMap<>();
        for (final BeanDefinition component : components) {
            final Class<?> cls = Class.forName(component.getBeanClassName());
            if (cls.equals(EnumSerializeAdapter.class)) {
                continue;
            }
            if (cls.isEnum()) {
                for (Object anEnum : EnumSet.allOf((Class) cls)) {
                    enumSerializes.computeIfAbsent((Class<Enum<?>>) cls, t -> new HashSet<>()).add((EnumSerialize<?>) anEnum);
                }
            } else {
                throw new UnsupportedOperationException("Class:" + cls.getCanonicalName() + "is not enum! " + "The class that implements the \"EnumSerialize\" must be an enumeration class.");
            }
        }
        return enumSerializes;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(ClassNotFoundException.class)
    private static Map<Class<Enum<?>>, Set<EnumSerialize<?>>> getAnnotatedEnums(ClassPathScanningCandidateComponentProvider provider, String path) {
        provider.resetFilters(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(CustomSerializationEnum.class));
        provider.addExcludeFilter(new AssignableTypeFilter(EnumSerialize.class));
        final Set<BeanDefinition> components = provider.findCandidateComponents(path);
        final Map<Class<Enum<?>>, Set<EnumSerialize<?>>> enumSerializes = new HashMap<>();
        for (final BeanDefinition component : components) {
            final Class<?> cls = Class.forName(component.getBeanClassName());
            if (cls.isEnum()) {
                for (Object anEnum : EnumSet.allOf((Class) cls)) {
                    enumSerializes.computeIfAbsent((Class<Enum<?>>) cls, t -> new HashSet<>()).add(new EnumSerializeAdapter((Enum<?>) anEnum));
                }
            } else {
                throw new UnsupportedOperationException("Class:" + cls.getCanonicalName() + "is not enum! " + "The class annotated by \"CustomSerializationEnum\" must be an enumeration class.");
            }
        }
        return enumSerializes;
    }
}