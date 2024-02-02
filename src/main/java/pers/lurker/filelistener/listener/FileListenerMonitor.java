package pers.lurker.filelistener.listener;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pers.lurker.filelistener.config.ListenerProperties;

import java.io.File;
import java.util.concurrent.ExecutorService;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileListenerMonitor extends FileAlterationListenerAdaptor {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private FileListener fileListener;
    private File observerDirectory;
    private ListenerProperties.Listener listener;
    private ExecutorService execPool;

    @Override
    public void onStart(FileAlterationObserver observer) {
        this.observerDirectory = observer.getDirectory();
    }

    @Override
    public void onFileCreate(File file) {
        execPool.execute(() -> {
            try {
                fileListener.onFileCreate(file, observerDirectory, listener);
                fileListener.onFileCreate(file, observerDirectory);
                fileListener.onFileCreate(file);
            } catch (Exception e) {
                log.error(getHandlerBeanName() + " 监听文件创建异常: " + file.getAbsolutePath(), e);
            }
        });
    }

    @Override
    public void onFileChange(File file) {
        execPool.execute(() -> {
            try {
                fileListener.onFileChange(file, observerDirectory, listener);
                fileListener.onFileChange(file, observerDirectory);
                fileListener.onFileChange(file);
            } catch (Exception e) {
                log.error(getHandlerBeanName() + " 监听文件变更异常: " + file.getAbsolutePath(), e);
            }
        });
    }

    @Override
    public void onFileDelete(File file) {
        execPool.execute(() -> {
            try {
                fileListener.onFileDelete(file, observerDirectory, listener);
                fileListener.onFileDelete(file, observerDirectory);
                fileListener.onFileDelete(file);
            } catch (Exception e) {
                log.error(getHandlerBeanName() + " 监听文件删除异常: " + file.getAbsolutePath(), e);
            }
        });
    }

    public FileListenerMonitor setFileListener(FileListener fileListener,
        ListenerProperties.Listener listener, ExecutorService execPool) {
        this.fileListener = fileListener;
        this.listener = listener;
        this.execPool = execPool;
        return this;
    }

    private String getHandlerBeanName() {
        return listener.getHandler() == null ? "" : listener.getHandler();
    }

}
