package com.muyuanjin;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import com.muyuanjin.annotating.EnumSerializeProxy;
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
import sun.misc.SharedSecrets;

import java.util.*;

/**
 * @author muyuanjin
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class TestConfig {
    private final Map<Class<Enum<?>>, Set<EnumSerialize<?>>> enumSerializes;

    public TestConfig(@Value("${custom-serialization-enum.path:'com'}") String path) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        enumSerializes = getEnumSerializes(provider, path);
        enumSerializes.putAll(getAnnotatedEnums(provider, path));
    }

    @Autowired
    @SuppressWarnings({"unchecked", "rawtypes"})
    void registryConverter(ConverterRegistry converterRegistry) {
        for (Map.Entry<Class<Enum<?>>, Set<EnumSerialize<?>>> classSetEntry : enumSerializes.entrySet()) {
            EnumSerialize<?> next = classSetEntry.getValue().iterator().next();
            CustomSerializationEnum annotation = next.getAnnotation();
            CustomSerializationEnum.Type type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.requestParam();
            converterRegistry.addConverter(String.class, (Class) next.getOriginalClass(), t -> type.getDeserializeObj((Class) next.getOriginalClass(), t));
        }
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.modules(new SimpleModule() {
            {
                for (Map.Entry<Class<Enum<?>>, Set<EnumSerialize<?>>> classSetEntry : enumSerializes.entrySet()) {
                    EnumSerialize<?> next = classSetEntry.getValue().iterator().next();
                    addDeserializer(next.getOriginalClass(), new CustomSerializationEnumJsonDeserializer(new Pair<>(classSetEntry.getKey(), classSetEntry.getValue())));
                    addSerializer(next.getOriginalClass(), new CustomSerializationEnumJsonSerializer(new Pair<>(classSetEntry.getKey(), classSetEntry.getValue())));
                }
            }
        });
    }

    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return t -> {
            for (Map.Entry<Class<Enum<?>>, Set<EnumSerialize<?>>> classSetEntry : enumSerializes.entrySet()) {
                EnumSerialize<?> next = classSetEntry.getValue().iterator().next();
                t.getTypeHandlerRegistry().register(next.getOriginalClass(), new CustomSerializationEnumTypeHandler(new Pair<>(classSetEntry.getKey(), classSetEntry.getValue())));
            }
        };
    }

    /**
     * 通过父类class和类路径获取该路径下父类的所有子类列表
     *
     * @return 所有该类子类或实现类的列表
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(ClassNotFoundException.class)
    private static Map<Class<Enum<?>>, Set<EnumSerialize<?>>> getEnumSerializes(ClassPathScanningCandidateComponentProvider provider, String path) {
        provider.resetFilters(false);
        provider.addIncludeFilter(new AssignableTypeFilter(EnumSerialize.class));
        final Set<BeanDefinition> components = provider.findCandidateComponents(path);
        final Map<Class<Enum<?>>, Set<EnumSerialize<?>>> enumSerializes = new HashMap<>();
        for (final BeanDefinition component : components) {
            final Class<EnumSerialize<?>> cls = (Class<EnumSerialize<?>>) Class.forName(component.getBeanClassName());
            if (cls.equals(EnumSerializeProxy.class)) {
                continue;
            }
            if (cls.isEnum()) {
                Arrays.stream(SharedSecrets.getJavaLangAccess().getEnumConstantsShared((Class) cls))
                        .forEach(e -> enumSerializes.computeIfAbsent((Class<Enum<?>>) ((EnumSerialize<?>) e).getOriginalClass(),
                                t -> new HashSet<>()).add((EnumSerialize<?>) e));
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
            final Class<Enum<?>> cls = (Class<Enum<?>>) Class.forName(component.getBeanClassName());
            if (cls.isEnum()) {
                Arrays.stream(SharedSecrets.getJavaLangAccess().getEnumConstantsShared((Class) cls))
                        .forEach(e ->
                                enumSerializes.computeIfAbsent((Class<Enum<?>>) e.getClass(), t -> new HashSet<>())
                                        .add(new EnumSerializeProxy((Enum<?>) e))
                        );
            } else {
                throw new UnsupportedOperationException("Class:" + cls.getCanonicalName() + "is not enum! " + "The class annotated by \"CustomSerializationEnum\" must be an enumeration class.");
            }
        }
        return enumSerializes;
    }

}
