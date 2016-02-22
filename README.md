# None parser plugin for Embulk

Embulk parser plugin not to parse at all

## Install

```
$ embulk gem install embulk-parser-none
```

## Overview

* **Plugin type**: parser
* **Guess supported**: no

## Configuration

- **column_name**: A column name which this plugin outputs (string, default: "payload")

## Example

```yaml
in:
  type: file
  path_prefix: example.txt
  parser:
    type: none
    column_name: payload
```

Assume the input file (example.txt) is as following:

```
foo bar baz
foo bar baz
```

then this plugin treats as:

```
+----------------+
| payload:string |
+----------------+
| foo bar baz    |
| foo bar baz    |
+----------------+
```

To recover a file, you may use [embulk-formatter-single_value](https://github.com/sonots/embulk-formatter-single_value) as:

```
out:
  type: file
  path_prefix: example.txt
  sequence_format: ""
  file_ext: .out
  formatter:
    type: single_value
```

or csv formatter as:

```
out:
  type: file
  path_prefix: example.txt
  sequence_format: ""
  file_ext: .out
  formatter:
    type: csv
    delimiter: 0
    quote_policy: NONE
    header_line: false
```

## ChangeLOG

[CHANGELOG.md](CHANGELOG.md)

## Development

Run example:

```
$ embulk gem install embulk-formatter-single_value
$ ./gradlew classpath
$ embulk run -I lib example.yml
```

Run test:

```
$ ./gradlew test
```

Release gem:

```
$ ./gradlew gemPush
```
