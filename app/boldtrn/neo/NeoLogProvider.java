package boldtrn.neo;

import org.neo4j.function.Consumer;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.Logger;

/**
 * Created by robin on 18/11/15.
 */
public class NeoLogProvider implements LogProvider {
    @Override
    public Log getLog(Class loggingClass) {
        return new NeoLog();
    }

    @Override
    public Log getLog(String name) {
        return new NeoLog();
    }

    public class NeoLog implements Log{

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public Logger debugLogger() {
            return new NeoLogger();
        }

        @Override
        public void debug(String message) {
            play.Logger.info(message);

        }

        @Override
        public void debug(String message, Throwable throwable) {
            play.Logger.error(message, throwable);

        }

        @Override
        public void debug(String format, Object... arguments) {
            play.Logger.info(String.format(format, arguments));

        }

        @Override
        public Logger infoLogger() {
            return new NeoLogger();
        }

        @Override
        public void info(String message) {
            play.Logger.info(message);

        }

        @Override
        public void info(String message, Throwable throwable) {
            play.Logger.error(message, throwable);

        }

        @Override
        public void info(String format, Object... arguments) {
            play.Logger.info(String.format(format, arguments));

        }

        @Override
        public Logger warnLogger() {
            return new NeoLogger();
        }

        @Override
        public void warn(String message) {
            play.Logger.info(message);

        }

        @Override
        public void warn(String message, Throwable throwable) {
            play.Logger.error(message, throwable);

        }

        @Override
        public void warn(String format, Object... arguments) {
            play.Logger.info(String.format(format, arguments));

        }

        @Override
        public Logger errorLogger() {
            return new NeoLogger();
        }

        @Override
        public void error(String message) {
            play.Logger.info(message);

        }

        @Override
        public void error(String message, Throwable throwable) {
            play.Logger.error(message, throwable);

        }

        @Override
        public void error(String format, Object... arguments) {
            play.Logger.info(String.format(format, arguments));

        }

        @Override
        public void bulk(Consumer<Log> consumer) {

        }
    }

    public class NeoLogger implements Logger
    {
        /**
         * @param message The message to be written
         */
        public void log( String message ){
            play.Logger.info(message);
        }

        /**
         * @param message   The message to be written
         * @param throwable An exception that will also be written
         */
        public void log( String message, Throwable throwable ){
            play.Logger.error(message, throwable);
        }

        /**
         * @param format    A string format for writing a message
         * @param arguments Arguments to substitute into the message according to the {@param format}
         */
        public void log( String format, Object... arguments ){
            play.Logger.info(String.format(format, arguments));
        }

        /**
         * Used to temporarily write several messages in bulk. The implementation may choose to
         * disable flushing, and may also block other operations until the bulk update is completed.
         *
         * @param consumer A callback operation that accepts an equivalent {@link Logger}
         */
        public void bulk( Consumer<Logger> consumer ){

        }
    }

}
