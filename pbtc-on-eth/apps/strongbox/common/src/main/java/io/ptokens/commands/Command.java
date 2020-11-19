package io.ptokens.commands;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.sqlite.database.sqlite.SQLiteDatabase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Vector;

import io.ptokens.database.SQLiteHelper;

/**
 * Represents a Strongbox command.
 *
 * This should help to build the Strongbox
 * method the cli intends to invoke by easily
 * specifying the function name and the list of
 * arguments.
 */
public class Command {
    private static final String TAG = Command.class.getName();
    private Vector<Object> valueArgs = new Vector<>();
    private Intent intent = null;
    private Class<?> callerClass;
    private Context context;
    private String marker;
    private String command;
    private boolean flagAsync = false;
    private boolean flagNative = false;
    private boolean flagWriteableDatabase = false;
    private boolean flagReadableDatabase = false;

    public Command(Context context, String command, String marker) {
        this.marker = marker;
        this.context = context;
        this.command = command;
        this.callerClass = context.getClass();
    }

    public Command(Context instance, Intent intent) {
        this(
                instance,
                intent.getStringExtra("command"),
                intent.getStringExtra("marker")
        );
        this.intent = intent;
    }

    public Command copy() {
        return new Command(context, intent);
    }

    /**
     * Read the intent extras with the given [argName] and
     * push it inside the [valueArgs] array, eventually
     * parsing it to the given [type].
     *
     *              ORDER IS IMPORTANT
     *
     * @param argName name of the parameter
     * @param argValueDefault value of the parameter
     * @param type the primitive type of the parameter (i.e String.class)
     * @return a valid Command instance
     */
    Command addIntentArg(
            String argName,
            String argValueDefault,
            Class type
    ) throws InvalidCommandException {
        if (intent != null) {
            String argValueStr = intent.getStringExtra(argName);
            argValueStr = argValueStr == null || argValueStr.isEmpty()
                    ? argValueDefault
                    : argValueStr;

            // Means the default value given is null.
            // This is a trick to raise an exception
            // when a parameter is required and can't have
            // a default value
            if (argValueStr == null) {
                Log.v(TAG,"✘ The value for " + argName + " is missing!");
                return this;
            }

            switch (type.getName()) {
                case "java.lang.String":
                    Log.v(TAG,"The value for " + argName + " set!");
                    addValueArg(argValueStr);
                    break;
                case "java.lang.Long":
                    addValueArg(Long.parseUnsignedLong(argValueStr));
                    break;
                case "java.lang.Integer":
                    addValueArg(Integer.parseUnsignedInt(argValueStr));
                    break;
                case "java.lang.Byte":
                    addValueArg(Byte.parseByte(argValueStr, 16));
                    break;
                case "java.lang.Boolean":
                    addValueArg(Boolean.parseBoolean(argValueStr));
                    break;
                default:
                    throw new InvalidCommandException(
                            "✘ Intent's extra type " + type.getName() + " not implemented"
                    );
            }
        } else {
            throw new InvalidCommandException("✘ Intent given is null");
        }

        return this;
    }

    /**
     * Using this we don't close the activity using
     * System.exit(0) but finish() (i.e. generating
     * the proof)
     */
    public Command async() {
        flagAsync = true;
        return this;
    }
    
    public Command needsWritableDatabase() {
        flagWriteableDatabase = true;
        return this;
    }

    public Command needsReadableDatabase() {
        flagReadableDatabase = true;
        return this;
    }
    /**
     * This would add a new parameter to the command.
     *
     *             ORDER IS IMPORTANT
     *
     * The coder should be careful to add them in the order
     * expected by the specified function (defined by command).
     * For example if the command is initializeEnclave, then
     * one argument is expected which is the json block.
     *
     * @param value method argument
     * @return a valid Command instance
     */
    public Command addValueArg(Object value) {
        valueArgs.add(value);
        return this;
    }

    public Command isNative() {
        flagNative = true;
        return this;
    }

    public boolean isAsync() {
        return flagAsync;
    }

    /**
     * Search for the method to launch when invokeMethod
     * is called.
     *
     * @return the method ready to be called
     */
    private Method searchMethodFromArgs() throws
            NoSuchMethodException,
            InvalidParameterException {

        ArrayList<Class<?>> parametersTypes = new ArrayList<>();

        // Every native method needs a Main class instance as 
        // first argument
        if (flagNative) {
            parametersTypes.add(callerClass); 
        }

        for (Object argument : valueArgs) {
            Class<?> clazz = argument.getClass();

            switch (clazz.getName()) {
                case "java.lang.Integer":
                    parametersTypes.add(int.class);
                    break;
                case "java.lang.Long":
                    parametersTypes.add(long.class);
                    break;
                case "java.lang.Byte":
                    parametersTypes.add(byte.class);
                    break;
                case "java.lang.Short":
                    parametersTypes.add(short.class);
                    break;
                case "java.lang.Boolean":
                    parametersTypes.add(boolean.class);
                    break;
                default:
                    parametersTypes.add(clazz);
            }
        }

        if (parametersTypes.isEmpty())
            return callerClass.getMethod(command);

        Class<?>[] parametersTypesArray = new Class[parametersTypes.size()];
        parametersTypes.toArray(parametersTypesArray);

        return callerClass.getMethod(command, parametersTypesArray);
    }

    public SQLiteDatabase getDatabase() {
        SQLiteHelper helper = new SQLiteHelper(context);
        SQLiteDatabase db = null;
        if (flagWriteableDatabase) {
            db = helper.getWritableDatabase();
        } else if (flagReadableDatabase) {
            db = helper.getReadableDatabase();
        }

        return db;
    }

    public Object invokeCommand() throws
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            InvalidCommandException {
        Method nativeMethod = searchMethodFromArgs();

        if (command == null) {
            throw new InvalidCommandException("✘ A command was expected");
        }

        if (marker == null) {
            throw new InvalidCommandException("✘ A marker was expected");
        }

        ArrayList<Object> arguments = new ArrayList<>();

        if (flagNative) {
            arguments.add(context);
        }

        arguments.addAll(valueArgs);

        Log.d(TAG, "✔ invokeCommand: invoking method "
            + nativeMethod 
            + " argument len " 
            + arguments.size()
        );

        return nativeMethod.invoke(
            context,
            arguments.toArray()
        );
    }

    public Context getContext() {
        return context;
    }
    public String getMarker() {
        return marker;
    }
    public String getCommand() {
        return command;
    }
}
