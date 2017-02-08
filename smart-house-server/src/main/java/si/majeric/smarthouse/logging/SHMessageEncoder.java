package si.majeric.smarthouse.logging;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import si.majeric.smarthouse.Environment;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SHMessageEncoder extends PatternLayoutEncoder {
	@Override
	public void doEncode(ILoggingEvent event) throws IOException {
		String txt = layout.doLayout(event);
		Messages.getInstance().add(txt);
	}

	public static final class Messages {
		private static Messages INSTANCE;
		private CircularFifoQueue<String> _messageQueue;

		private Messages(int size) {
			_messageQueue = new CircularFifoQueue<String>(size);
		}

		public static Messages getInstance() {
			if (INSTANCE == null) {
				INSTANCE = new Messages(Environment.getLogMessagesSize());
			}
			return INSTANCE;
		}

		public synchronized boolean add(String message) {
			return _messageQueue.add(message);
		}

		/**
		 * 
		 * @return copy of messages queue
		 */
		public synchronized List<String> getMessages() {
			return new LinkedList<String>(_messageQueue);
		}
	}
}
