package pers.filelistener.listener;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileListenerMonitor extends FileAlterationListenerAdaptor {

    private FileListener fileListener;
    private File observerDirectory;

    @Override
    public void onStart(FileAlterationObserver observer) {
        this.observerDirectory = observer.getDirectory();
    }

    @Override
    public void onFileCreate(File file) {
        fileListener.onFileCreate(file, observerDirectory);
        fileListener.onFileCreate(file);
    }

    @Override
    public void onFileChange(File file) {
        fileListener.onFileChange(file, observerDirectory);
        fileListener.onFileChange(file);
    }

    @Override
    public void onFileDelete(File file) {
        fileListener.onFileDelete(file, observerDirectory);
        fileListener.onFileDelete(file);
    }

    public FileListenerMonitor setFileListener(FileListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }
}
