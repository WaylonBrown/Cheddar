package cheddar.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.fd.HoundSearchResult;
import com.hound.android.fd.Houndify;
import com.hound.android.libphs.PhraseSpotterReader;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.core.model.sdk.CommandResult;
import com.hound.core.model.sdk.HoundResponse;

import java.util.Locale;

/**
 * Created by waylon.brown on 4/8/16.
 */
public class HoundifyManager {
    private Context context;
    private TextToSpeechMgr textToSpeechMgr;
    private PhraseSpotterReader phraseSpotterReader;
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private MatchedResponseListener listener;

    public interface MatchedResponseListener {
        void onMatchedResponse(String key);
    }

    public HoundifyManager(Context context, MatchedResponseListener listener) {
        this.context = context;
        this.listener = listener;
        textToSpeechMgr = new TextToSpeechMgr((Activity)context);
    }

    /**
     * Called to start the Phrase Spotter
     */
    public void startPhraseSpotting() {
        if (phraseSpotterReader == null) {
            phraseSpotterReader = new PhraseSpotterReader(new SimpleAudioByteStreamSource());
            phraseSpotterReader.setListener(phraseSpotterListener);
            phraseSpotterReader.start();
        }
    }

    /**
     * Called to stop the Phrase Spotter
     */
    public void stopPhraseSpotting() {
        if (phraseSpotterReader != null) {
            phraseSpotterReader.stop();
            phraseSpotterReader = null;
        }
    }

    public void stopTextToSpeechManager() {
        textToSpeechMgr.shutdown();
        textToSpeechMgr = null;
    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
        final HoundSearchResult result = Houndify.get(context).fromActivityResult(resultCode, data);

        if (result.hasResult()) {
            onResponse( result.getResponse() );
        }
        else if (result.getErrorType() != null) {
            //error
        }
    }

    private void onResponse(final HoundResponse response) {
        if (response.getResults().size() > 0) {
            // Required for conversational support
            StatefulRequestInfoFactory.get(context).setConversationState(response.getResults().get(0).getConversationState());
            textToSpeechMgr.speak(response.getResults().get(0).getSpokenResponse());

            if ( response.getResults().size() > 0 ) {
                CommandResult commandResult = response.getResults().get( 0 );
                if ( commandResult.getCommandKind().equals("ClientMatchCommand")) {
                    JsonNode matchedItemNode = commandResult.getJsonNode().findValue("MatchedItem");
                    String intentValue = matchedItemNode.findValue( "Intent").textValue();

                    if (!TextUtils.isEmpty(intentValue)) {
                        listener.onMatchedResponse(intentValue);
                    }
                }
            }
        }
    }

    /**
     * Implementation of the PhraseSpotterReader.Listener interface used to handle PhraseSpotter
     * call back.
     */
    private final PhraseSpotterReader.Listener phraseSpotterListener = new PhraseSpotterReader.Listener() {
        @Override
        public void onPhraseSpotted() {

            // It's important to note that when the phrase spotter detects "Ok Hound" it closes
            // the input stream it was provided.
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopPhraseSpotting();
                    // Now start the HoundifyVoiceSearchActivity to begin the search.
                    Houndify.get(context).voiceSearch((Activity)context);
                }
            });
        }

        @Override
        public void onError(final Exception ex) {

            // for this sample we don't care about errors from the "Ok Hound" phrase spotter.

        }
    };

    /**
     * Helper class used for managing the TextToSpeech engine
     */
    class TextToSpeechMgr implements TextToSpeech.OnInitListener {
        private TextToSpeech textToSpeech;

        public TextToSpeechMgr( Activity activity ) {
            textToSpeech = new TextToSpeech( activity, this );
        }

        @Override
        public void onInit( int status ) {
            // Set language to use for playing text
            if ( status == TextToSpeech.SUCCESS ) {
                textToSpeech.setLanguage(Locale.US);
            }
        }

        public void shutdown() {
            textToSpeech.shutdown();
        }
        /**
         * Play the text to the device speaker
         *
         * @param textToSpeak
         */
        public void speak( String textToSpeak ) {
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null);
        }
    }
}
