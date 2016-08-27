// Copyright 2015 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ninja.eivind.hotsreplayuploader.files.tempwatcher;

import ninja.eivind.hotsreplayuploader.services.platform.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class TempReplayWatcher implements InitializingBean, DisposableBean {

    public static final String DIRECTORY_NAME = "Heroes of the Storm";
    private static final Logger logger = LoggerFactory.getLogger(TempReplayWatcher.class);
    private final PlatformService platformService;
    private final File heroesDirectory;
    private final File tempDirectory;
    private Thread watchThread;
    private BattleLobbyWatcher battleLobbyWatcher;

    @Autowired
    public TempReplayWatcher(PlatformService platformService) {
        this.platformService = platformService;
        tempDirectory = platformService.getTempDirectory();
        heroesDirectory = new File(tempDirectory, "Heroes of the Storm");
    }


    @Override
    public void afterPropertiesSet() throws Exception {


        start();
    }

    private void start() {
        logger.info("Starting TempReplayWatcher...");
        Runnable runnable = getRunnable(tempDirectory);
        watchThread = new Thread(runnable);
        watchThread.start();
        battleLobbyWatcher = new BattleLobbyWatcher(heroesDirectory);
        if (heroesDirectory.exists()) {
            battleLobbyWatcher.start();
        }
    }

    @SuppressWarnings("unchecked")
    private Runnable getRunnable(File tempDirectory) {
        Path path = tempDirectory.toPath();
        return () -> {
            logger.info("TempReplayWatcher started.");
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
                while (true) {
                    WatchKey key = watchService.take();
                    key.pollEvents().forEach(event -> {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) {
                            return;
                        }
                        final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        final Path pathName = pathEvent.context();
                        String fileName = new File(path.toFile(), pathName.toString()).getName();
                        if (fileName.contains(DIRECTORY_NAME)) {
                            if (kind == ENTRY_CREATE) {
                                logger.info("Discovered Heroes of the Storm folder");
                                battleLobbyWatcher.start();
                            } else if (kind == ENTRY_DELETE) {
                                battleLobbyWatcher.stop();
                            }
                        }
                    });
                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (IOException e) {
                logger.error("Watcher threw exception", e);
            } catch (InterruptedException e) {
                logger.info("TempReplayWatcher was interrupted. Winding down.", e);
            }
        };
    }


    @Override
    public void destroy() throws Exception {
        stop();
    }

    private void stop() {
        logger.info("Stopping TempReplayWatcher");
        watchThread.interrupt();
        watchThread = null;
    }
}
