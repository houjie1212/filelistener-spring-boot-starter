package pers.lurker.filelistener.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import pers.lurker.filelistener.listener.FileListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

public class Finder extends SimpleFileVisitor<Path> {

    private static final Logger log = LoggerFactory.getLogger(Finder.class);
    private final List<PathAttrs> resultList = new ArrayList<>();
    private boolean includeHidden;
    private String[] suffixFilter;
    private Pattern regexMatcher;

    private FileListener fileListener;
    private File observerDirectory;
    private ListenerProperties.Listener prop;
    private ExecutorService execPool;

    public Finder setIncludeHidden(boolean includeHidden) {
        this.includeHidden = includeHidden;
        return this;
    }

    public Finder setSuffixFilter(String[] suffixFilter) {
        this.suffixFilter = suffixFilter;
        return this;
    }

    public Finder setRegexMatcher(String pattern) {
        if (StringUtils.hasText(pattern)) {
            regexMatcher = Pattern.compile(pattern);
        }
        return this;
    }

    public Finder setFileListener(FileListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }

    public Finder setObserverDirectory(File observerDirectory) {
        this.observerDirectory = observerDirectory;
        return this;
    }

    public Finder setProp(ListenerProperties.Listener prop) {
        this.prop = prop;
        return this;
    }

    public Finder setExecPool(ExecutorService execPool) {
        this.execPool = execPool;
        return this;
    }

    /**
     * 返回结果文件集合，按文件目录和创建时间排序
     */
    public List<File> getResultFiles() {
        return resultList.stream()
            .sorted(Comparator.comparing(pa -> ((PathAttrs) pa).getPath().getParent())
                .thenComparing(pa -> ((PathAttrs) pa).getAttrs().creationTime())
            ).map(pa -> pa.getPath().toFile())
            .collect(Collectors.toList());
    }

    /**
     * 使用比较器进行匹配 文件或则目录的名称
     */
    void find(Path file, BasicFileAttributes attrs) throws IOException {
        Path name = file.getFileName();
        if (name != null) {
            String fileName = name.toFile().getName();
            if (!includeHidden) {
                if (Files.isHidden(file)) {
                    return;
                }
            }
            if (suffixFilter != null && suffixFilter.length > 0) {
                if (Arrays.stream(suffixFilter).noneMatch(
                    s -> s.equalsIgnoreCase(fileName.substring(fileName.lastIndexOf(".") + 1)))
                ) {
                    return;
                }
            }
            if (regexMatcher != null && !regexMatcher.matcher(fileName).find()) {
                return;
            }

            if (prop.getLoadOnStartMode() == 0) {
                execPool.execute(() -> {
                    try {
                        this.fileListener.onStart(file.toFile(), observerDirectory, prop);
                        this.fileListener.onStart(file.toFile(), observerDirectory);
                        this.fileListener.onStart(file.toFile());
                    } catch (Exception e) {
                        log.error(String.format("启动扫描文件异常,mode:0, file: %s", file.toAbsolutePath()), e);
                    }
                });
            } else {
                resultList.add(new PathAttrs(file, attrs));
            }
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (!includeHidden) {
            if (Files.isHidden(dir)) {
                return SKIP_SUBTREE;
            }
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            find(file, attrs);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        if (exc instanceof FileSystemLoopException) {
            log.error("cycle detected: " + file);
        } else {
            log.error(String.format("Unable to copy:" + " %s: %s", file, exc));
        }
        return CONTINUE;
    }

    public static class PathAttrs {
        private final Path path;
        private final BasicFileAttributes attrs;

        public PathAttrs(Path path, BasicFileAttributes attrs) {
            this.path = path;
            this.attrs = attrs;
        }

        public Path getPath() {
            return path;
        }

        public BasicFileAttributes getAttrs() {
            return attrs;
        }
    }
}
