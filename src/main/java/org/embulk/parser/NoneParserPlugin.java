package org.embulk.parser;

import org.embulk.util.config.Config;
import org.embulk.util.config.ConfigDefault;
import org.embulk.config.ConfigSource;
import org.embulk.util.config.ConfigMapper;
import org.embulk.util.config.ConfigMapperFactory;
import org.embulk.util.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.ParserPlugin;
import org.embulk.spi.FileInput;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.util.config.TaskMapper;
import org.embulk.util.config.units.SchemaConfig;

import org.embulk.spi.Exec;
import org.embulk.spi.PageBuilder;
import org.embulk.util.text.LineDecoder;
import org.embulk.util.config.units.ColumnConfig;
import org.embulk.util.text.LineDelimiter;
import org.embulk.util.text.Newline;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Optional;

import static org.embulk.spi.type.Types.STRING;

public class NoneParserPlugin
        implements ParserPlugin
{
    private static final ConfigMapperFactory CONFIG_MAPPER_FACTORY = ConfigMapperFactory
            .builder()
            .addDefaultModules()
            .build();
    private static final ConfigMapper CONFIG_MAPPER = CONFIG_MAPPER_FACTORY.createConfigMapper();

    public interface PluginTask
            extends Task
    {
        @Config("column_name")
        @ConfigDefault("\"payload\"")
        public String getColumnName();

        // From org.embulk.spi.util.LineDecoder.DecoderTask
        @Config("charset")
        @ConfigDefault("\"utf-8\"")
        public Charset getCharset();

        // From org.embulk.spi.util.LineDecoder.DecoderTask
        @Config("newline")
        @ConfigDefault("\"CRLF\"")
        public Newline getNewline();

        // From org.embulk.spi.util.LineDecoder.DecoderTask.
        @Config("line_delimiter_recognized")
        @ConfigDefault("null")
        Optional<LineDelimiter> getLineDelimiterRecognized();

    }

    @Override
    public void transaction(ConfigSource config, ParserPlugin.Control control)
    {
        final PluginTask task = CONFIG_MAPPER.map(config, PluginTask.class);
        ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        final String columnName = task.getColumnName();

        columns.add(new ColumnConfig(columnName, STRING ,config));

        Schema schema = new SchemaConfig(columns).toSchema();

        // TODO: Use task.toTaskSource() after dropping v0.9
        control.run(task.dump(), schema);
    }

    @Override
    public void run(TaskSource taskSource, Schema schema,
            FileInput input, PageOutput output)
    {
        final TaskMapper taskMapper = CONFIG_MAPPER_FACTORY.createTaskMapper();
        final PluginTask task = taskMapper.map(taskSource, PluginTask.class);

        LineDecoder lineDecoder = LineDecoder.of(input, task.getCharset(), task.getLineDelimiterRecognized().orElse(null));
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
