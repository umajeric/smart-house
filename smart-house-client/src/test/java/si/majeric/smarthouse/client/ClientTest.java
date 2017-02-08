package si.majeric.smarthouse.client;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.HouseDao;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.dao.SwitchDao;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.House;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.xstream.XStreamSupport;

/**
 * @author Uros Majeric
 */
public class ClientTest extends TestCase {
    static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    @Override
    protected void setUp() throws Exception {
    }

    public void testTrigger() throws Exception {
        final TestCaseClient client = new TestCaseClient();
        client.init();
        client.downloadHouseConfig();

        final House house = client.getHouse();
        assertNotNull(house);
        assertNotNull(house.getConfiguration());

        final List<Floor> floors = house.getConfiguration().getFloors();
        for (Floor floor : floors) {
            if ("Pritličje".equals(floor.getName())) {
                final List<Room> rooms = floor.getRooms();
                for (Room room : rooms) {
                    if ("Kabinet".equals(room.getName())) {
                        final List<Switch> switches = room.getSwitches();
                        for (Switch swtch : switches) {
                            if ("KabinetLuc".equals(swtch.getId())) {
                                final List<TriggerConfig> triggers = swtch.getTriggers();
                                if (triggers != null && !triggers.isEmpty()) {
                                    triggers.get(0).setTest("xxx");
                                    String xml = new XStreamSupport().serialize(triggers.get(0));
                                    client.invokeStateChange(triggers.get(0), new ResponseListener() {
                                        @Override
                                        public void stateChanged(Map<String, PinState> swtch) {
                                            logger.info("State changed...");
                                        }

                                        @Override
                                        public void errorChangingState(Throwable t) {
                                            logger.error("Error changing state: " + t.getMessage());
                                        }
                                    }, false);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private class TestCaseClient extends Client {

        private TestCaseClient() {
            super(new TestCaseSmartHouseDaoFactory());
        }

        @Override
        protected String getHost() {
            return "localhost";
        }

        @Override
        protected int getPort() {
            return 8000;
        }

        @Override
        protected String getUsername() {
            return "sh-username";
        }

        @Override
        protected String getPassword() {
            return "sh-password";
        }
    }

    private class TestCaseSmartHouseDaoFactory implements SmartHouseDaoFactory {

        @Override
        public HouseDao getHouseDao() {
            return null;
        }

        @Override
        public ConfigurationDao getConfigurationDao() {
            return new TestCaseConfigurationDao();
        }

        @Override
        public SwitchDao getSwitchDao() {
            return null;
        }
    }

    private class TestCaseConfigurationDao implements ConfigurationDao {

        @Override
        public Configuration loadDefault() {
            return null;
        }

        @Override
        public Configuration findFor(House house) {
            return null;
        }

        @Override
        public Configuration findById(String id) {
            return null;
        }

        @Override
        public Configuration save(Configuration configuration) {
            return null;
        }

        @Override
        public int remove(Configuration entity) {
            return 0;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public List<Configuration> findAll() {
            return null;
        }
    }
}
