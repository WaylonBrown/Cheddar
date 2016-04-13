package cheddar.util;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.core.model.sdk.ClientMatch;
import com.hound.core.model.sdk.HoundRequestInfo;

import java.util.ArrayList;

public class StatefulRequestInfoFactory extends DefaultRequestInfoFactory {

    public static StatefulRequestInfoFactory instance;
    private JsonNode conversationState;

    public static final String KEY_SELFIE = "SELFIE_PLEASE";
    public static final String KEY_TIMER_5 = "TIMER_5";
    public static final String KEY_TIMER_10 = "TIMER_10";
    public static final String KEY_TIMER_15 = "TIMER_15";
    public static final String KEY_FLIP_CAM = "FLIP_CAM";

    public static StatefulRequestInfoFactory get(final Context context) {
        if (instance == null) {
            instance= new StatefulRequestInfoFactory(context);
        }
        return instance;
    }

    private StatefulRequestInfoFactory(Context context) {
        super(context);
    }

    public void setConversationState(JsonNode conversationState) {
        this.conversationState = conversationState;
    }

    @Override
    public HoundRequestInfo create() {
        final HoundRequestInfo requestInfo = super.create();
        requestInfo.setConversationState(conversationState);
        ArrayList<ClientMatch> clientMatchList = new ArrayList<>();

        clientMatchList.add(makeClientMatch("\"Selfie please\"", "Smile!", KEY_SELFIE));
        clientMatchList.add(makeClientMatch("\"Selfie in 5 seconds\"", "Smile, taking in 5 seconds!", KEY_TIMER_5));
        clientMatchList.add(makeClientMatch("\"Selfie in 10 seconds\"", "Smile, taking in 10 seconds!", KEY_TIMER_10));
        clientMatchList.add(makeClientMatch("\"Selfie in 15 seconds\"", "Smile, taking in 15 seconds!", KEY_TIMER_15));
        clientMatchList.add(makeClientMatch("\"Flip the camera\"", "Camera flipped", KEY_FLIP_CAM));

        // add the list of matches to the request info object
        requestInfo.setClientMatches(clientMatchList);

        return requestInfo;
    }

    private ClientMatch makeClientMatch(String expression, String response, String key) {
        final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ClientMatch clientMatch = new ClientMatch();
        clientMatch.setExpression(expression);

        clientMatch.setSpokenResponse(response);
        clientMatch.setSpokenResponseLong(response);
        clientMatch.setWrittenResponse(response);
        clientMatch.setWrittenResponseLong(response);

        ObjectNode result1Node = nodeFactory.objectNode();
        result1Node.put("Intent", key);
        clientMatch.setResult(result1Node);

        return clientMatch;
    }
}