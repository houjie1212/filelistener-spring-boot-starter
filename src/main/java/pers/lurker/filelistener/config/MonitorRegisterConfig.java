package pers.lurker.filelistener.config;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
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
import pers.lurker.filelistener.listener.FileListener;
import pers.lurker.filelistener.listener.FileListenerMonitor;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ListenerProperties.class)
@ComponentScan("pers.lurker.filelistener")
public class MonitorRegisterConfig implements ApplicationContextAware {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;

    private final ListenerProperties listenerProperties;

    private ExecutorService loadOnStartExecPool = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    public MonitorRegisterConfig(ListenerProperties listenerProperties) {
        this.listenerProperties = listenerProperties;
    }

    @PostConstruct
    private void register() {
        List<ListenerProperties.Listener> props = listenerProperties.getListeners();
        log.debug("listeners.size:{}", props.size());
        for (ListenerProperties.Listener prop : props) {
            if (StringUtils.hasText(prop.getAddress())) {

                for (String address : prop.getAddress().split(",")) {
                    File directory = new File(address);
                    if (directory.isFile()) {
                        log.warn("加载目录[{}]失败", address);
                        continue;
                    }
                    if (!directory.exists()) {
                        directory.mkdirs();
                        log.info("{}不存在，已自动创建", address);
                    }

                    new Thread(() -> {
                        // 启动时扫描目录
                        List<File> files = loadFileOnStart(directory, prop, listenerProperties);
                        // 处理启动时扫描目录返回的文件
                        handleFilesFromLoadOnStart(files, directory, prop);
                    }).start();

                    new Thread(() -> {
                        // 开启目录监听
                        startFileListener(directory, prop);
                    }).start();

                }
            }
        }
    }

    private List<File> loadFileOnStart(File directory, ListenerProperties.Listener prop,
        ListenerProperties listenerProperties) {
        boolean loadOnStart = prop.getLoadOnStart() == null ?
            listenerProperties.isLoadOnStart() : prop.getLoadOnStart();
        if (!loadOnStart) {
            return null;
        }
        log.info("--->读取目录开始[{}](suffix:{}, handler:{}，matchingConditions:{})",
            directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions());

        Finder finder = new Finder();
        finder.setFileListener(getFileListener(prop.getHandler()))
            .setObserverDirectory(directory)
            .setProp(prop)
            .setExecPool(loadOnStartExecPool);
        finder.setIncludeHidden(prop.isIncludeHidden());
        String[] extensionsFilter = StringUtils.hasText(prop.getSuffix()) ?
            Arrays.stream(prop.getSuffix().split(","))
                .filter(StringUtils::hasText)
                .toArray(String[]::new)
            : null;
        finder.setSuffixFilter(extensionsFilter);
        if (StringUtils.hasText(prop.getMatchingConditions())) {
            finder.setRegexMatcher(prop.getMatchingConditions());
        }
        int maxDepth = prop.isRecursive() ? Integer.MAX_VALUE : 1;
        try {
            Files.walkFileTree(directory.toPath(), Collections.emptySet(), maxDepth, finder);
        } catch (IOException e) {
            log.error(String.format("读取目录开始[%s]异常", directory.getAbsolutePath()), e);
            return null;
        }
        log.info("<---读取目录结束[{}](suffix:{}, handler:{}，matchingConditions:{})",
            directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions());
        return finder.getResultFiles();
    }

    private void handleFilesFromLoadOnStart(List<File> files, File directory,
        ListenerProperties.Listener prop) {
        if (files == null || prop.getLoadOnStartMode() == 0) {
            return;
        }
        if (files.isEmpty()) {
            log.info("读取目录结果文件为空[{}](suffix:{}, handler:{}，matchingConditions:{})",
                directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions());
        } else {
            loadOnStartExecPool.execute(() -> {
                log.info("--->处理读取目录结果文件开始[{}](suffix:{}, handler:{}，matchingConditions:{})",
                    directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions());
                FileListener fileListener = getFileListener(prop.getHandler());
                try {
                    fileListener.onStart(files, directory, prop);
                    fileListener.onStart(files, directory);
                    fileListener.onStart(files);
                } catch (Exception e) {
                    log.error(String.format("启动扫描文件异常,mode:1, directory: %s", directory), e);
                }
                log.info("<---处理读取目录结果文件结束[{}](suffix:{}, handler:{}，matchingConditions:{})",
                    directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions());
            });
        }
    }

    private void startFileListener(File directory, ListenerProperties.Listener prop) {
        try {
            log.info("准备监听目录[{}](suffix:{}, handler:{}，matchingConditions:{})",
                directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions());
            FileAlterationMonitor monitor = getMonitor(directory, prop);
            monitor.start();
            log.info("开始监听目录[{}](suffix:{}, handler:{}，matchingConditions:{})",
                directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions());
        } catch (Exception e) {
            log.error(String.format("监听目录失败[%s](suffix:%s, handler:%s，matchingConditions:%s)",
                directory.getAbsolutePath(), prop.getSuffix(), prop.getHandler(), prop.getMatchingConditions()
            ), e);
        }
    }

    private FileAlterationMonitor getMonitor(File directory, ListenerProperties.Listener prop) {
        long interval = TimeUnit.SECONDS.toMillis(prop.getInterval());

        List<IOFileFilter> filtersOr = new ArrayList<>();
        List<IOFileFilter> filtersAnd = new ArrayList<>();
        if (prop.isRecursive()) {
            IOFileFilter directories = FileFilterUtils.and(FileFilterUtils.directoryFileFilter());
            filtersOr.add(directories);
        }

        if (StringUtils.hasText(prop.getSuffix())) {
            String[] suffixs = prop.getSuffix().split(",");

            filtersAnd.add(FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                FileFilterUtils.or(Arrays.stream(suffixs)
                    .filter(StringUtils::hasText)
                    .map(s -> FileFilterUtils.suffixFileFilter(s, IOCase.INSENSITIVE))
                    .toArray(IOFileFilter[]::new)
                )));
        }

        if (StringUtils.hasText(prop.getMatchingConditions())) {
            IOFileFilter regexFileFilter = new RegexFileFilter(prop.getMatchingConditions(), IOCase.INSENSITIVE);
            filtersAnd.add(FileFilterUtils.and(FileFilterUtils.fileFileFilter(), regexFileFilter));
        }

        if (filtersAnd.size() <= 0) {
            filtersOr.add(FileFilterUtils.and(FileFilterUtils.fileFileFilter()));
        }

        IOFileFilter filter = FileFilterUtils.and(filtersAnd.toArray(new IOFileFilter[0]));
        if (!filtersOr.isEmpty()) {
            filtersOr.add(filter);
            filter = FileFilterUtils.or(filtersOr.toArray(new IOFileFilter[0]));
        }

        FileAlterationObserver observer = new FileAlterationObserver(directory, filter);

        observer.addListener(
            applicationContext.getBean(FileListenerMonitor.class)
                .setFileListener(getFileListener(prop.getHandler()), prop, loadOnStartExecPool)
        );

        return new FileAlterationMonitor(interval, observer);
    }

    /**
     * 从spring容器获取FileListener实例
     * @param handler
     * @return
     */
    private FileListener getFileListener(String handler) {
        return StringUtils.isEmpty(handler) ?
            applicationContext.getBean(FileListener.class)
            : applicationContext.getBean(handler, FileListener.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
