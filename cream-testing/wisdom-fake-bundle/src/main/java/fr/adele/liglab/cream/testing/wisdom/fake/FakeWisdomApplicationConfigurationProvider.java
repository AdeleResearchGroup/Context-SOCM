package fr.adele.liglab.cream.testing.wisdom.fake;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
@Provides
@Instantiate
public class FakeWisdomApplicationConfigurationProvider implements ApplicationConfiguration{
    @Override
    public File getBaseDir() {
        return null;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public boolean isTest() {
        return false;
    }

    @Override
    public boolean isProd() {
        return false;
    }

    @Override
    public File getFileWithDefault(String s, String s1) {
        return null;
    }

    @Override
    public File getFileWithDefault(String s, File file) {
        return null;
    }

    @Override
    public Configuration getConfiguration(String s) {
        return null;
    }

    @Override
    public boolean has(String s) {
        return false;
    }

    @Override
    public String get(String s) {
        return null;
    }

    @Override
    public <T> T get(String s, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> T getOrDie(String s, Class<T> aClass) {
        return null;
    }

    @Override
    public <T> T get(String s, Class<T> aClass, T t) {
        return null;
    }

    @Override
    public <T> T get(String s, Class<T> aClass, String s1) {
        return null;
    }

    @Override
    public String getWithDefault(String s, String s1) {
        return null;
    }

    @Override
    public Integer getInteger(String s) {
        return null;
    }

    @Override
    public Integer getIntegerWithDefault(String s, Integer integer) {
        return null;
    }

    @Override
    public Double getDouble(String s) {
        return null;
    }

    @Override
    public Double getDoubleWithDefault(String s, Double aDouble) {
        return null;
    }

    @Override
    public Boolean getBoolean(String s) {
        return null;
    }

    @Override
    public Boolean getBooleanWithDefault(String s, Boolean aBoolean) {
        return null;
    }

    @Override
    public Long getLong(String s) {
        return null;
    }

    @Override
    public Long getLongWithDefault(String s, Long aLong) {
        return null;
    }

    @Override
    public Long getLongOrDie(String s) {
        return null;
    }

    @Override
    public Boolean getBooleanOrDie(String s) {
        return null;
    }

    @Override
    public Integer getIntegerOrDie(String s) {
        return null;
    }

    @Override
    public Double getDoubleOrDie(String s) {
        return null;
    }

    @Override
    public String getOrDie(String s) {
        return null;
    }

    @Override
    public Long getDuration(String s, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Long getDuration(String s, TimeUnit timeUnit, long l) {
        return null;
    }

    @Override
    public Long getBytes(String s) {
        return null;
    }

    @Override
    public Long getBytes(String s, long l) {
        return null;
    }

    @Override
    public String[] getStringArray(String s) {
        return new String[0];
    }

    @Override
    public List<String> getList(String s) {
        return null;
    }

    @Override
    public Properties asProperties() {
        return null;
    }

    @Override
    public Map<String, Object> asMap() {
        return null;
    }
}
