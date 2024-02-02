package pers.lurker.filelistener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "file.listener")
public class ListenerProperties {

    private boolean loadOnStart;
    private List<Listener> listeners = new ArrayList<>();

    public static class Listener {
        private String address;
        private String handler;
        private String suffix;
        private boolean recursive;
        private Long interval = 5L;
        private Boolean loadOnStart;
        /**
         * 启动扫描目录模式，默认0
         * 0: 立即返回，调用单文件处理方法 FileListener.onStart(File, [...])
         * 1: 集合返回，按文件创建时间排序，调用文件集合处理方法 FileListener.onStart(Collection<File>, [...])
         */
        private int loadOnStartMode;
        /**正则匹配表达式*/
        private String matchingConditions;
        private boolean includeHidden;
        private Map<String, String> properties = new HashMap<>();

        public String getAddress() {
            return address;
        }

        public Listener setAddress(String address) {
            this.address = address;
            return this;
        }

        public String getHandler() {
            return handler;
        }

        public Listener setHandler(String handler) {
            this.handler = handler;
            return this;
        }

        public String getSuffix() {
            return suffix;
        }

        public Listener setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public boolean isRecursive() {
            return recursive;
        }

        public Listener setRecursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public Long getInterval() {
            return interval;
        }

        public Listener setInterval(Long interval) {
            this.interval = interval;
            return this;
        }

        public Boolean getLoadOnStart() {
            return loadOnStart;
        }

        public Listener setLoadOnStart(Boolean loadOnStart) {
            this.loadOnStart = loadOnStart;
            return this;
        }

        public int getLoadOnStartMode() {
            return loadOnStartMode;
        }

        public Listener setLoadOnStartMode(int loadOnStartMode) {
            this.loadOnStartMode = loadOnStartMode;
            return this;
        }

        public String getMatchingConditions() {
            return matchingConditions;
        }

        public Listener setMatchingConditions(String matchingConditions) {
            this.matchingConditions = matchingConditions;
            return this;
        }

        public boolean isIncludeHidden() {
            return includeHidden;
        }

        public Listener setIncludeHidden(boolean includeHidden) {
            this.includeHidden = includeHidden;
            return this;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public Listener setProperties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }
    }

    public boolean isLoadOnStart() {
        return loadOnStart;
    }

    public ListenerProperties setLoadOnStart(boolean loadOnStart) {
        this.loadOnStart = loadOnStart;
        return this;
    }

    public List<Listener> getListeners() {
        return listeners;
    }

    public ListenerProperties setListeners(List<Listener> listeners) {
        this.listeners = listeners;
        return this;
    }
}
