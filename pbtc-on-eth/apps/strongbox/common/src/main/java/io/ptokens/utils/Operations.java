package io.ptokens.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

public class Operations {

    public static final String TAG = Operations.class.getName();

    public static String readFile(String name) {
        File dataDirectory = new File("/data/local/tmp");

        Log.v(TAG, "✔ Reading " + name + " from " + dataDirectory);
        File fileToRead = new File(dataDirectory, name);

        //Read text from file
        String line;
        StringBuilder text = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(fileToRead))) {
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "✘ Failed to read the file " + fileToRead, e);
            return "";
        }

        return text.toString();
    }

    public static void writeBytes(Context context, String fileName, byte[] signedState) {
        try (FileOutputStream outputStream = context.openFileOutput(fileName, MODE_PRIVATE)){
            outputStream.write(signedState);
            outputStream.flush();
            Log.i(TAG, "✔ " + fileName + " written to disk");
        } catch (IOException e) {
            Log.e(TAG, "✘ Failed to write " + fileName + " to disk:", e);
        }
    }

    public static byte[] readBytes(Context context, String fileName) {
        byte[] ba = {};
        try (FileInputStream inputStream = context.openFileInput(fileName)){
            ba = IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            Log.w(TAG, "✔ Failed to get the signature from the file, ok if first run!");
        }
        return ba;
    }
}
