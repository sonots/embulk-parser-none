package org.embulk.parser;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.ParserPlugin;
import org.embulk.spi.FileInput;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfig;

import org.embulk.spi.Exec;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.util.LineDecoder;
//import org.embulk.spi.type.TimestampType;
//import org.embulk.spi.time.TimestampParser;
//import org.embulk.spi.time.TimestampParseException;
import org.embulk.spi.ColumnConfig;
import java.util.ArrayList;

//import static org.embulk.spi.type.Types.BOOLEAN;
//import static org.embulk.spi.type.Types.DOUBLE;
//import static org.embulk.spi.type.Types.LONG;
import static org.embulk.spi.type.Types.STRING;
//import static org.embulk.spi.type.Types.TIMESTAMP;

public class NoneParserPlugin
        implements ParserPlugin
{
    public interface PluginTask
            extends Task, LineDecoder.DecoderTask //, TimestampParser.Task
    {
        @Config("message_key")
        @ConfigDefault("\"message\"")
        public String getMessageKey();
    }

    @Override
    public void transaction(ConfigSource config, ParserPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);
        ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        final String messageKey = task.getMessageKey();

        columns.add(new ColumnConfig(messageKey, STRING ,config));

        Schema schema = new SchemaConfig(columns).toSchema();
        control.run(task.dump(), schema);
    }

    @Override
    public void run(TaskSource taskSource, Schema schema,
            FileInput input, PageOutput output)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        LineDecoder lineDecoder = new LineDecoder(input,task);
        PageBuilder pageBuilder = new PageBuilder(Exec.getBufferAllocator(), schema, output);
        String line = null;
        final String messageKey = task.getMessageKey();

        while( input.nextFile() ){
            while(true){
              line = lineDecoder.poll();

              if( line == null ){
                  break;
              }

              pageBuilder.setString(0, line);
              pageBuilder.addRecord();
            }
        }
        pageBuilder.finish();
    }
}
