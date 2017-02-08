package si.majeric.smarthouse.excel.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.model.Address;
import si.majeric.smarthouse.model.Address.Pin;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Cron;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.House;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.Switch.NotificationType;
import si.majeric.smarthouse.model.Switch.SwitchType;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.model.TriggerConfig.GpioTriggerType;
import si.majeric.smarthouse.xstream.dao.SmartHouseConfigReadError;

public class ExcelConfigurationDao implements ConfigurationDao {
	private static final Logger logger = LoggerFactory.getLogger(ExcelConfigurationDao.class);

	private static final int COLUMN_FLOOR_NAME = 0;
	private static final int COLUMN_ROOM_NAME = 1;
	private static final int COLUMN_SWITCH_ID = 2;
	private static final int COLUMN_SWITCH_NAME = 3;
	private static final int COLUMN_SWITCH_PROVIDER_ADDRESS = 4;
	private static final int COLUMN_SWITCH_PIN = 5;
	private static final int COLUMN_SWITCH_TYPE = 6;
	private static final int COLUMN_TRIGGER_NAME = 7;
	private static final int COLUMN_TRIGGER_PROVIDER_ADDRESS = 8;
	private static final int COLUMN_TRIGGER_PIN = 9;
	private static final int COLUMN_TRIGGER_TYPE = 10;
	private static final int COLUMN_TRIGGER_PARAMS = 11;
	private static final int COLUMN_TRIGGER_CRON = 12;
	private static final int COLUMN_SWITCH_NOTIFICATION = 13;

	public ExcelConfigurationDao() {
	}

	@Override
	public Configuration loadDefault() {
		return findById(Environment.getConfigModelFile());
	}

	@Override
	public Configuration findFor(House house) {
		return findById(house != null ? house.getId() : null);
	}

	@Override
	public Configuration findById(String fileName) {
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);

			/* Get the workbook instance for XLS file */
			XSSFWorkbook workbook = new XSSFWorkbook(is);

			/* Get first sheet from the workbook */
			XSSFSheet sheet = workbook.getSheet("Konfiguracija");

			if (sheet == null) {
				throw new RuntimeException("Missing 'Konfiguracija' sheet in the excel file.");
			}

			Configuration conf = new Configuration();
			conf.setName("Hiša");
			Iterator<Row> rowIterator = sheet.iterator();

			if (!rowIterator.hasNext()) {
				return conf;
			}

			/* skip first row (titles) */
			rowIterator.next();

			/* store already added trigger IDs - we will throw exception if one already exists */
			List<String> triggerConfigIds = new ArrayList<String>();

			/* Iterate through each rows from first sheet */
			List<Floor> floors = conf.getFloors();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getCell(0) != null) {
					String cellValue = row.getCell(0).getStringCellValue();
					/* do not read further if first cell in the row contains 'END' string */
					if ("END".equals(cellValue)) {
						break;
					} else if (cellValue.startsWith("#")) {
						/* skip rows where the first cell value starts with '#' */
						continue;
					}
				}

				/* get the floor */
				Cell floorNameCell = row.getCell(COLUMN_FLOOR_NAME);
				if (floorNameCell == null || floorNameCell.getStringCellValue() == null || floorNameCell.getStringCellValue().isEmpty()) {
					continue;
				}
				String floorName = floorNameCell.getStringCellValue();
				Floor floor = new Floor();
				floor.setId(normalize(floorName));
				floor.setName(floorName);
				int floorIndex = floors.indexOf(floor);
				/* if floor not in the list yet, then add it */
				if (floorIndex < 0) {
					floors.add(floor);
				} else {
					floor = floors.get(floorIndex);
				}

				List<Room> rooms = floor.getRooms();
				String roomName = row.getCell(COLUMN_ROOM_NAME).getStringCellValue();
				Room room = new Room();
				room.setId(normalize(roomName));
				room.setName(roomName);

				int roomIndex = rooms.indexOf(room);
				/* if room not in the list yet, then add it */
				if (roomIndex < 0) {
					rooms.add(room);
				} else {
					room = rooms.get(roomIndex);
				}

				List<Switch> switches = room.getSwitches();
				Switch swtch = new Switch();
				swtch.setId(row.getCell(COLUMN_SWITCH_ID).getStringCellValue());
				int switchIndex = switches.indexOf(swtch);
				/* if room not in the list yet, then add it */
				if (switchIndex < 0) {
					swtch.setName(row.getCell(COLUMN_SWITCH_NAME).getStringCellValue());
					final Cell switchTypeCell = row.getCell(COLUMN_SWITCH_TYPE);
					swtch.setType(SwitchType.valueOf(switchTypeCell.getStringCellValue()));
					Cell notificationCell = row.getCell(COLUMN_SWITCH_NOTIFICATION);
					if (notificationCell != null && notificationCell.getStringCellValue() != null && !notificationCell.getStringCellValue().isEmpty()) {
						swtch.setNotificationType(NotificationType.valueOf(notificationCell.getStringCellValue()));
					}

					Address address = new Address();
					address.setProviderAddress((int) row.getCell(COLUMN_SWITCH_PROVIDER_ADDRESS).getNumericCellValue());

					Cell pinCell = row.getCell(COLUMN_SWITCH_PIN);
					if (pinCell != null && pinCell.getStringCellValue() != null && !pinCell.getStringCellValue().isEmpty()) {
						Pin pin = Pin.valueOf(pinCell.getStringCellValue());
						address.setPin(pin);
						swtch.setAddress(address);
					}

					switches.add(swtch);
				} else {
					/* else get the existing one - we will add only one more trigger */
					swtch = switches.get(switchIndex);
				}

				/* finally create the trigger */
				List<TriggerConfig> triggers = swtch.getTriggers();

				TriggerConfig trgConf = new TriggerConfig();
				trgConf.setDefault(switchIndex < 0);
				trgConf.setName(row.getCell(COLUMN_TRIGGER_NAME).getStringCellValue());
				trgConf.setType(GpioTriggerType.valueOf(row.getCell(COLUMN_TRIGGER_TYPE).getStringCellValue()));

				/* set the duration */
				Cell params = row.getCell(COLUMN_TRIGGER_PARAMS);
				if (GpioTriggerType.PULSE.equals(trgConf.getType()) || GpioTriggerType.TPULSE.equals(trgConf.getType())) {
					if (params != null) {
						trgConf.setDuration((long) params.getNumericCellValue());
					} else {
						trgConf.setDuration(30000l);
					}
				} else if (GpioTriggerType.SET.equals(trgConf.getType())) {
					if (params != null) {
						trgConf.setNewState((int) params.getNumericCellValue() == 0 ? PinState.LOW : PinState.HIGH);
					} else {
						trgConf.setNewState(PinState.HIGH);
					}
				} else if (GpioTriggerType.PUSH.equals(trgConf.getType())) {
					if (params != null) {
						// trgConf.setDescription(params.getStringCellValue()); // TODO
					}
				} else if (GpioTriggerType.SOUND.equals(trgConf.getType())) {
					if (params != null) {
						 trgConf.setExtra(params.getStringCellValue());
					}
				}

				Address address = new Address();
				Cell trgProviderAddress = row.getCell(COLUMN_TRIGGER_PROVIDER_ADDRESS);
				/* if there is no provider address then this is only a trigger for the smart client */
				if (trgProviderAddress != null && trgProviderAddress.getNumericCellValue() > 0) {
					address.setProviderAddress((int) trgProviderAddress.getNumericCellValue());
					Cell pinCell = row.getCell(COLUMN_TRIGGER_PIN);
					if (pinCell != null && pinCell.getStringCellValue() != null && !pinCell.getStringCellValue().isEmpty()) {
						Pin pin = Pin.valueOf(pinCell.getStringCellValue());
						address.setPin(pin);
						trgConf.setAddress(address);
					}
					// trgConf.setId(address.getProviderAddress() + "/" + address.getPin().toString());
				} else {
					// trgConf.setId(replaceSumniki(trgConf.getName()).replace(" ", "_"));
				}

				trgConf.setId(swtch.getId() + normalize(trgConf.getName()));
				if (triggerConfigIds.contains(trgConf.getId())) {
					throw new RuntimeException("Multiple triggers with the same ID: '" + trgConf.getId() + "' (switch: "
							+ swtch.getAddress() + ")");
				}
				triggerConfigIds.add(trgConf.getId());

				Cell cronCell = row.getCell(COLUMN_TRIGGER_CRON);
				if (cronCell != null) {
					Cron cron = new Cron();
					cron.setExpression(cronCell.getStringCellValue());
					cron.setManualTriggerEnabled(true);
					trgConf.setCron(cron);
				}

				triggers.add(trgConf);

			}

			return conf;
		} catch (Exception e) {
			throw new SmartHouseConfigReadError(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	@Override
	public Configuration save(Configuration conf) {
		throw new UnsupportedOperationException("Cpnfiguration dos not support saving to Excel yet.");
	}

	@Override
	public int remove(Configuration entity) {
		return 0;
	}

	@Override
	public long count() {
		return 1;
	}

	@Override
	public List<Configuration> findAll() {
		ArrayList<Configuration> list = new ArrayList<Configuration>();
		list.add(findById(Environment.getConfigModelFile()));
		return list;
	}

	protected String normalize(String string) {
		return string.replaceAll("š", "s").replaceAll("č", "c").replaceAll("ž", "z").replaceAll("Č", "C").replaceAll("Š", "S")
				.replaceAll("Ž", "Z").replace(" ", "_").replace("'", "");
	}

	public static void main(String[] args) throws IOException {
		String file = "/Users/uros/workspaces/as/SmartHouse/smart-house-xstream-impl/src/test/resources/konfiguracija-hisa.xlsx";
		final Configuration byId = new ExcelConfigurationDao().findById(file);
		logger.info(byId.toString());
	}

}
