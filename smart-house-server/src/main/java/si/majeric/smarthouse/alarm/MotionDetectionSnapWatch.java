package si.majeric.smarthouse.alarm;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.SmartHouse;
import si.majeric.smarthouse.cron.CronTriggerScheduler;
import si.majeric.smarthouse.exception.TriggerNotConfiguredException;
import si.majeric.smarthouse.mail.Mail;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.pi.PiSmartHouse;
import si.majeric.smarthouse.push.AbstractPush;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * @author Uros Majeric
 */
public class MotionDetectionSnapWatch implements Runnable {
    static final Logger logger = LoggerFactory.getLogger(MotionDetectionSnapWatch.class);
    final Path path;
    private final SmartHouse smartHouse;

    public MotionDetectionSnapWatch(SmartHouse smartHouse, Path path) {
        this.smartHouse = smartHouse;
        this.path = path;
    }

    @Override
    public void run() {
        // Sanity check - Check if path is a folder
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path, "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        } catch (IOException ioe) {
            logger.error("Folder does not exists", ioe);
            return;
        }

        logger.info("Watching for changes...  {}", path);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try {
            Files.walkFileTree(path, new DirectoryCleaner(path)); // clear or register the folder

            final WatchService service = fs.newWatchService();

            final NewFileHandler fileHandler = new NewFileHandler();
            Files.walkFileTree(path, new DirectoryRegisterer(service, fileHandler)); // register watch for all existing folders

            final Thread fileHandlerThread = new Thread(fileHandler);
            fileHandlerThread.setDaemon(true);
            fileHandlerThread.start();


            // Start the infinite polling loop
            WatchKey key = null;
            while (true) {
                try {
                    key = service.take();

                    Kind<?> kind = null;
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        // Get the type of the event
                        kind = watchEvent.kind();
                        if (OVERFLOW == kind) {
                            continue;
                        } else if (ENTRY_CREATE == kind) {
                            // A new Path was created
                            final Path fullPath = ((Path) key.watchable()).resolve((Path) watchEvent.context());
                            final Boolean isFolder = (Boolean) Files.getAttribute(fullPath, "basic:isDirectory", NOFOLLOW_LINKS);
                            if (isFolder) {
                                Files.walkFileTree(fullPath, new DirectoryRegisterer(service, fileHandler));
                                continue;
                            }
                            fileHandler.handle(fullPath);
                        }
                    }

                } catch (IOException ioe) {
                    logger.error(ioe.getLocalizedMessage(), ioe);
                } catch (InterruptedException ie) {
                    logger.error(ie.getLocalizedMessage(), ie);
                }
                if (!key.reset()) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private class DirectoryRegisterer extends SimpleFileVisitor<Path> {
        private final WatchService service;
        private final NewFileHandler fileHandler;

        private DirectoryRegisterer(WatchService service, NewFileHandler fileHandler) {
            this.service = service;
            this.fileHandler = fileHandler;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            dir.register(service, ENTRY_CREATE);
            logger.info("Watching for changes: {}", dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//            fileHandler.handle(file);
            return FileVisitResult.CONTINUE;
        }
    }

    private class DirectoryCleaner extends SimpleFileVisitor<Path> {
        private final Path path;

        private DirectoryCleaner(Path path) {
            this.path = path;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            deleteOldFile(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            // if directory not equals root dir and is empty then delete it
            if (!path.equals(dir) && dir.toFile().list().length == 0) {
                deleteOldFile(dir);
            }
            return FileVisitResult.CONTINUE;
        }

        private void deleteOldFile(Path path) {
            try {
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                final long diff = new Date().getTime() - attr.creationTime().toMillis();
                final int days = 1;
                if (diff > days * 24 * 60 * 60 * 1000) {
                    // delete file older than "days"
                    Files.delete(path);
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private class NewFileHandler implements Runnable {
        private Queue<Path> filesQueue = new ConcurrentLinkedQueue<>();
        private final SunriseSunsetCalculator _calculator;
        private final Map<Switch, Long> _switchLastTrigger = new HashMap<>();

        NewFileHandler() {
            Location location = new Location(Environment.getLatitude(), Environment.getLongitude());
            _calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());
        }

        public void handle(Path path) {
            logger.info("handle: " + path);
            filesQueue.add(path);
        }

        public void run() {
            try {
                final String mailTo = Environment.getProperty("mail.to");
                String[] to = null;
                if (mailTo != null) {
                    to = mailTo.split(";");
                } else {
                    logger.error("mail.to is not defined - motion detection will be disabled");
                }
                while (true) {
                    try {
                        if (filesQueue.size() > 0) {
                            Path path = filesQueue.poll();
                            if (path != null) {
                                final boolean alarmOn = Alarm.getInstance().isOn();
                                final Calendar time = Calendar.getInstance(TimeZone.getDefault());
                                logger.info("Video Move Detected (alarm: " + alarmOn + " / " + time.get(Calendar.HOUR_OF_DAY) + "): " + path);

                                if (alarmOn || time.get(Calendar.HOUR_OF_DAY) > 22 || time.get(Calendar.HOUR_OF_DAY) < 7) {
                                    // TODO send push notification when this happens
                                    // AbstractPush.getDefaultImpl().sendPush("{\"data\": \"Motion Detection triggered\", \"type\":\"AlarmTrigger\"}");
                                    if (to != null) {
                                        final ArrayList<Path> attachments = new ArrayList<>();
                                        attachments.add(path);
                                        Mail.sendEmail(to, "Video motion detected", "", attachments);
                                    }
                                }
                                Calendar sunrise = _calculator.getOfficialSunriseCalendarForDate(time);
                                Calendar sunset = _calculator.getOfficialSunsetCalendarForDate(time);

                                // trigger switch only if out is dark :-)
                                if (smartHouse.getConfiguration() != null && (time.before(sunrise) || time.after(sunset))) {
                                    for (Floor floor : smartHouse.getConfiguration().getFloors()) {
                                        for (Room room : floor.getRooms()) {
                                            // if name of the room is in the path (we could get this from room config or something)
                                            if (path.toString().contains(room.getName())) {
                                                for (Switch swtch : room.getSwitches()) {
                                                    for (TriggerConfig tc : swtch.getTriggers()) {
                                                        final long duration = 180000l;
                                                        if (tc.getType() == TriggerConfig.GpioTriggerType.PULSE && tc.getDuration() == duration) {
                                                            // if light was triggered 3 minutes ago (+10 seconds) then it turned of now (most probably) - so ignore this event
                                                            final boolean lightTurnedOff = CronTriggerScheduler.wasTriggeredInTimeframeBetween(swtch, duration, duration + 10000);
                                                            if (!lightTurnedOff) {
                                                                PinState state = smartHouse.getProvisionedPinState(swtch);
                                                                // if switch is not already ON then turn on the light
                                                                if (state != PinState.HIGH) {

                                                                    CronTriggerScheduler.notifySwitchTriggeredExpectedly(swtch);
                                                                    smartHouse.invokeTrigger(tc);
                                                                }
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Thread.sleep(1000); // wait for one second (if new file comes in between)
                        }
                    } catch (MessagingException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    } catch (TriggerNotConfiguredException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                }
            } catch (InterruptedException se) {
                logger.error("InterruptedException: {}", se.getMessage());
                se.printStackTrace();
            } catch (Exception se) {
                logger.error("Exception: {}", se.getMessage());
                se.printStackTrace();
            } finally {
                logger.info("Leaving run Method");
            }
        }
    }

}
