package m.c.m.proxyma.log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
     * This custom formatter that "doesn't format" anything.
     * In othed words the messages received from this formatter have to be
     * pre formatted externally.
     */
public class OnlyTheMessageFormatter extends Formatter {

    /**
     * Initialize parent class.
     */
    public OnlyTheMessageFormatter () {
        super();
    }

    // This method is called for every log records and puts out only the given message
    @Override
    public String format(LogRecord rec) {
        return rec.getMessage();
    }

    // This method is called just after the handler using this formatter is created
    @Override
    public String getHead(Handler h) {
        return "";
    }
    // This method is called just after the handler using this formatter is closed

    @Override
    public String getTail(Handler h) {
        return "";
    }
}
