package si.majeric.smarthouse.http;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

import si.majeric.smarthouse.SmartHouse;
import si.majeric.smarthouse.cron.CronTriggerScheduler;
import si.majeric.smarthouse.exception.TriggerNotConfiguredException;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

/**
 * Created by Uros Majeric on 05/12/16.
 */
public class LightsSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(LightsSpeechlet.class);
    private final SmartHouse smartHouse;

    public LightsSpeechlet(SmartHouse smartHouse) {
        this.smartHouse = smartHouse;
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session) throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("LightsIntent".equals(intentName)) {
            return  getLightsResponse(intent);
        }
        return getHelpResponse();
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session) throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Where?";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     * @param intent
     */
    private SpeechletResponse getLightsResponse(Intent intent) {

        if (intent.getSlot("Room") == null || intent.getSlot("Room").getValue() == null) {
            return newAskResponse("", "Where would you like to do that?");
        }

        String speechText = "";
        String state = null; // null | On | Off
        if (intent.getSlot("State") != null && intent.getSlot("State").getValue() != null) {
            state = intent.getSlot("State").getValue();
            speechText += "Turning " + state + " ";
        } else {
            speechText += "Switching ";
        }
        speechText += "light ";
        speechText += "in " + intent.getSlot("Room").getValue() + " ";
        if (intent.getSlot("Duration") != null && intent.getSlot("Duration").getValue() != null) {
            final Duration duration = Duration.parse(intent.getSlot("Duration").getValue());
            speechText += "for " + duration.toMinutes() + " minutes";
        }

        new Thread(new LightSwitcher(intent.getSlot("Room").getValue(), state)).start();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Light");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private class LightSwitcher implements Runnable {

        private final String roomName;
        private final TriggerConfig.GpioTriggerType state;

        LightSwitcher(String roomName, String state) {
            this.roomName = roomName;
            if (state != null) {
                if ("on".equalsIgnoreCase(state)) {
                    this.state = TriggerConfig.GpioTriggerType.PULSE;
                } else if ("off".equalsIgnoreCase(state)) {
                    this.state = TriggerConfig.GpioTriggerType.SET;
                } else {
                    this.state = TriggerConfig.GpioTriggerType.TPULSE;
                }
            } else {
                this.state = TriggerConfig.GpioTriggerType.TPULSE;
            }
        }

        @Override
        public void run() {
            if (smartHouse.getConfiguration() != null) {
                final boolean all = "All".equalsIgnoreCase(roomName) || "House".equalsIgnoreCase(roomName);
                for (Floor floor : smartHouse.getConfiguration().getFloors()) {
                    final boolean wholeFloor = roomName.equalsIgnoreCase(floor.getName());
                    for (Room room : floor.getRooms()) {
                        // if name of the room is in the path (we could get this from room config or something)
                        if (roomName.equalsIgnoreCase(room.getName()) || all || wholeFloor) {
                            for (Switch swtch : room.getSwitches()) {
                                if ("Luč".equalsIgnoreCase(swtch.getName())) {
                                    for (TriggerConfig tc : swtch.getTriggers()) {
                                        if (tc.getType() == state) {
                                            try {
                                                smartHouse.invokeTrigger(tc);
                                            } catch (Exception e) {
                                                log.error(e.getLocalizedMessage(), e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say hello to me!";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Wrapper for creating the Ask response from the input strings with
     * plain text output and reprompt speeches.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        return newAskResponse(stringOutput, false, repromptText, false);
    }


    /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param isOutputSsml
     *            whether the output text is of type SSML
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml
     *            whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
                                             String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }
}