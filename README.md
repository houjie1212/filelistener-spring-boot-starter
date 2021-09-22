# 监听目录文件

## Maven Dependency
Add dependency.
```xml
<dependency>
    <groupId>pers</groupId>
    <artifactId>filelistener-spring-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

## Useage
### properties
```properties
file:
  listener:
    load-on-start: # 全局配置程序启动时是否扫描目录，默认false
    listeners:
      -
        address: # 监听目录
        handler: # 监听处理类bean name，只有1个时可以省略
        suffix: # 监听文件扩展名，多个“,”分隔，为空则监听所有文件
        recursive: # 是否递归，默认false
        interval: # 监听间隔，单位：秒，默认5
        load-on-start: # 程序启动时是否扫描目录，默认null
```

### handler

```java
import pers.filelistener.listener.FileListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;

@Component
public class CustomFileListener implements FileListener {
    @Override
    public void onStart(Collection<File> files, File observerDirectory) {
        // TODO
    }

    @Override
    public void onFileCreate(File file, File observerDirectory) {
        // TODO
    }

    @Override
    public void onFileChange(File file, File observerDirectory) {
        // TODO
    }

    @Override
    public void onFileDelete(File file) {
        // TODO
    }
}
```