package si.majeric.smarthouse.dao.mock;

import java.util.ArrayList;
import java.util.List;

import si.majeric.smarthouse.dao.HouseDao;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.House;
import si.majeric.smarthouse.model.HouseAccess;
import si.majeric.smarthouse.model.Room;

public class MockHouseDao implements HouseDao {

	@Override
	public House findById(String id) {
		House house = new House();
		HouseAccess houseAccess = new HouseAccess();
		houseAccess.setHost("192.168.1.210");
		houseAccess.setPort(8000);
		houseAccess.setName("LAN");
		house.getAccesses().add(houseAccess);

		Configuration configuration = new Configuration();
		configuration.setName("My House");
		house.setConfiguration(configuration);

		Floor floor = newFloor("Pritličje");
		configuration.getFloors().add(floor);

		// Room room = newRoom("Dnevna soba", 1);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Kuhinja", 2);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta šank", SwitchType.UPDOWN));
		// room.getSwitches().add(newSwitch("Roleta", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Kabinet", 3);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta šank", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Hodnik", 4);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Jedilnica", 5);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta", SwitchType.UPDOWN));
		// room.getSwitches().add(newSwitch("Roleta terasa", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Stopnišče", 6);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// floor.getRooms().add(room);
		//
		// floor.getRooms().add(newRoom("Kopalnica", 7));
		// floor.getRooms().add(newRoom("Kurilnica", 8));
		//
		// floor = newFloor("Mansarda");
		// configuration.getFloors().add(floor);
		//
		// room = newRoom("Spalnica", 1);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Soba 1", 2);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Soba 2", 3);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Soba 3", 4);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// room.getSwitches().add(newSwitch("Roleta", SwitchType.UPDOWN));
		// floor.getRooms().add(room);
		//
		// room = newRoom("Hodnik", 5);
		// room.getSwitches().add(newSwitch("Luč", SwitchType.SWITCH));
		// floor.getRooms().add(room);

		return house;
	}

	// private Switch newSwitch(String name, SwitchType type) {
	// Switch swtch = new Switch();
	// swtch.setId(name);
	// swtch.setName(name);
	// swtch.setType(type);
	// return swtch;
	// }

	private Room newRoom(String name, Integer sequence) {
		Room room = new Room();
		room.setName(name);
		room.setSequence(sequence);
		return room;
	}

	private Floor newFloor(String name) {
		Floor floor = new Floor();
		floor.setName(name);
		return floor;
	}

	@Override
	public House save(House house) {
		/* deliberate no-op */
		return house;
	}

	@Override
	public long count() {
		return 1;
	}

	@Override
	public List<House> findAll() {
		ArrayList<House> list = new ArrayList<House>();
		list.add(findById(null));
		return list;
	}

	@Override
	public int remove(House entity) {
		return 1;
	}

}
