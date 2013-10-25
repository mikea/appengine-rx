package com.mikea.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append(
                String.format(
                        "%s %tT %s : %s",
                        record.getLevel().getName().substring(0, 1),
                        new Date(record.getMillis()),
                        record.getLoggerName(),
                        record.getMessage()));
        Throwable e = record.getThrown();
        if (e != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            e.printStackTrace(writer);
            writer.flush();
            builder.append("\n");
            builder.append(stringWriter.toString());
        }
        builder.append("\n");
        return builder.toString();
    }
}