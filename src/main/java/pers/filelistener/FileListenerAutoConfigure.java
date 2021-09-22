package pers.filelistener;

import pers.filelistener.config.MonitorRegisterConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MonitorRegisterConfig.class})
public class FileListenerAutoConfigure {
}
