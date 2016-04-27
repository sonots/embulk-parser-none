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
import org.embulk.spi.ColumnConfig;
import java.util.ArrayList;

import static org.embulk.spi.type.Types.STRING;

public class NoneParserPlugin
        implements ParserPlugin
{
    public interface PluginTask
            extends Task, LineDecoder.DecoderTask //, TimestampParser.Task
    {
        @Config("column_name")
        @ConfigDefault("\"payload\"")
        public String getColumnName();
    }

    @Override
    public void transaction(ConfigSource config, ParserPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);
        ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        final String columnName = task.getColumnName();

        columns.add(new ColumnConfig(columnName, STRING ,config));

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
        final String columnName = task.getColumnName();

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
