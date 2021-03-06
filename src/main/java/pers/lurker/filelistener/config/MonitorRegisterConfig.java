package pers.lurker.filelistener.config;

import pers.lurker.filelistener.listener.FileListener;
import pers.lurker.filelistener.listener.FileListenerMonitor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ListenerProperties.class)
@ComponentScan("pers.lurker.filelistener")
public class MonitorRegisterConfig implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(MonitorRegisterConfig.class);
    private ApplicationContext applicationContext;

    private final ListenerProperties listenerProperties;

    public MonitorRegisterConfig(ListenerProperties listenerProperties) {
        this.listenerProperties = listenerProperties;
    }

    @PostConstruct
    private void register() {
        List<ListenerProperties.Listener> listeners = listenerProperties.getListeners();
        log.debug("listeners.size:{}", listeners.size());
        for (ListenerProperties.Listener listener : listeners) {
            if (StringUtils.hasText(listener.getAddress())) {

                for (String address : listener.getAddress().split(",")) {
                    File directory = new File(address);
                    if (directory.isFile()) {
                        log.warn("????????????[{}]??????", address);
                        continue;
                    }
                    if (!directory.exists()) {
                        directory.mkdirs();
                        log.info("{}???????????????????????????", address);
                    }

                    loadFileOnStart(directory, listener, listenerProperties);
                    startFileListener(directory, listener);
                }
            }
        }
    }

    private void loadFileOnStart(File directory, ListenerProperties.Listener listener, ListenerProperties listenerProperties) {
        boolean loadOnStart = listener.getLoadOnStart() == null ? listenerProperties.isLoadOnStart() : listener.getLoadOnStart();
        if (loadOnStart) {
            FileListener fileListener = getFileListener(listener.getHandler());

            String[] extensionsFilter = StringUtils.hasText(listener.getSuffix()) ? listener.getSuffix().split(",") : null;
            Collection<File> files = FileUtils.listFiles(directory, extensionsFilter, listener.isRecursive());

            log.info("--->??????????????????[{}](suffix:{}, handler:{})", directory.getAbsolutePath(), listener.getSuffix(), listener.getHandler());
            fileListener.onStart(files, directory);
            fileListener.onStart(files);
            log.info("<---??????????????????[{}](suffix:{}, handler:{})", directory.getAbsolutePath(), listener.getSuffix(), listener.getHandler());
        }
    }

    private void startFileListener(File directory, ListenerProperties.Listener listener) {
        FileAlterationMonitor monitor = getMonitor(directory, listener);
        try {
            monitor.start();
            log.info("??????????????????[{}](suffix:{}, handler:{})", directory.getAbsolutePath(), listener.getSuffix(), listener.getHandler());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private FileAlterationMonitor getMonitor(File directory, ListenerProperties.Listener listener) {
        long interval = TimeUnit.SECONDS.toMillis(listener.getInterval());

        List<IOFileFilter> filters = new ArrayList<>();
        if (listener.isRecursive()) {
            IOFileFilter directories = FileFilterUtils.and(FileFilterUtils.directoryFileFilter());
            filters.add(directories);
        }

        if (StringUtils.hasText(listener.getSuffix())) {
            String[] suffixs = listener.getSuffix().split(",");
            for (String s : suffixs) {
                IOFileFilter suffixFilter = FileFilterUtils.suffixFileFilter(s, IOCase.INSENSITIVE);
                filters.add(FileFilterUtils.and(FileFilterUtils.fileFileFilter(), suffixFilter));
            }
        } else {
            filters.add(FileFilterUtils.and(FileFilterUtils.fileFileFilter()));
        }

        IOFileFilter filter = FileFilterUtils.or(filters.toArray(new IOFileFilter[0]));
        FileAlterationObserver observer = new FileAlterationObserver(directory, filter);

        observer.addListener(
                applicationContext.getBean(FileListenerMonitor.class).setFileListener(getFileListener(listener.getHandler()))
        );
        return new FileAlterationMonitor(interval, observer);
    }

    /**
     * ???spring????????????FileListener??????
     * @param handler
     * @return
     */
    private FileListener getFileListener(String handler) {
        return StringUtils.isEmpty(handler) ?
                applicationContext.getBean(FileListener.class) : applicationContext.getBean(handler, FileListener.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
