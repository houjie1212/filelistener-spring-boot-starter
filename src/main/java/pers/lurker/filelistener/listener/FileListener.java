package pers.lurker.filelistener.listener;

import pers.lurker.filelistener.config.ListenerProperties;

import java.io.File;
import java.util.Collection;

/**
 * 文件监听方法定义，由应用服务实现
 */
public interface FileListener {

    default void onStart(Collection<File> files, File observerDirectory, ListenerProperties.Listener listener) {
    }
    default void onStart(Collection<File> files, File observerDirectory) {
    }

    default void onStart(Collection<File> files) {
    }

    default void onStart(File file, File observerDirectory, ListenerProperties.Listener listener) {
    }

    default void onStart(File file, File observerDirectory) {
    }

    default void onStart(File file) {
    }

    default void onFileCreate(File file, File observerDirectory, ListenerProperties.Listener listener) {
    }

    default void onFileCreate(File file, File observerDirectory) {
    }

    default void onFileCreate(File file) {
    }

    default void onFileChange(File file, File observerDirectory, ListenerProperties.Listener listener) {
    }

    default void onFileChange(File file, File observerDirectory) {
    }

    default void onFileChange(File file) {
    }

    default void onFileDelete(File file, File observerDirectory, ListenerProperties.Listener listener) {
    }

    default void onFileDelete(File file, File observerDirectory) {
    }

    default void onFileDelete(File file) {
    }
}
