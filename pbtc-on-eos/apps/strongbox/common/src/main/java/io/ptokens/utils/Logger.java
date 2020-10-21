package io.ptokens.utils;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import io.ptokens.commands.Command;
import io.ptokens.data.Error;

public class Logger {
    private static final String TAG = Logger.class.getName();

    private Command builder;
    private static final String RESULT_LOG_FILENAME = "result.log";
    private static final String RESULT_LOG_ABS_PATH = "/sdcard/";

    public Logger(Context context, Command command) {
        this.builder = command;
    }

    public void logError(String error, int error_code) {
        Error e = new Error(error, error_code);

        ObjectMapper om = new ObjectMapper();
        try {
            logResult(om.writeValueAsString(e));
        } catch (JsonProcessingException ex) {
            Log.e(TAG,"Failed to serialized error object", ex);
        }
    }

    public void logResult(String result) {
        String resultPlusMarker = "{RETMSG:"
                + builder.getCommand() + ":" + builder.getMarker()
                + "}"
                + result;

        Log.i(TAG, resultPlusMarker);
        String p = Paths
                .get(RESULT_LOG_ABS_PATH, RESULT_LOG_FILENAME)
                .toString();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(p, true))){
                out.write(resultPlusMarker.concat("\n"));
                // Flush isn't needed as it's automatically called in close()
        } catch (IOException e) {
            Log.e(TAG, "✘ logResult: Failed to create the file writer", e);
        }
    }

    public void logErrorAndExit(
            String functionName,
            String message,
            int errorCode
    ) {
        Log.e(TAG, functionName + ": ✘ " + message);
        logError(message, errorCode);
        System.exit(errorCode);
    }
}
