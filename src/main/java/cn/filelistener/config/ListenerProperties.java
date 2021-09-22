package cn.filelistener.config;

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
