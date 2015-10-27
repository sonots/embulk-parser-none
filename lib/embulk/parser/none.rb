Embulk::JavaPlugin.register_parser(
  "none", "org.embulk.parser.NoneParserPlugin",
  File.expand_path('../../../../classpath', __FILE__))
