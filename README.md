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

- **message_key**: A column name which this plugin outputs (string, default: "message")

## Example

```yaml
in:
  type: any file input plugin type
  parser:
    type: parser
    format: none
    message_key: message
```

Assume inputs are as following:

```
foo,bar,baz
foo,bar,baz
```

then this plugin treats as:

```
+----------------+
| message:string |
+----------------+
| foo,bar,baz    |
| foo,bar,baz    |
+----------------+
```

## See also

You may use [embulk-formatter-single_value](https://github.com/sonots/embulk-formatter-single_value) to recover outputs

## ChangeLOG

[CHANGELOG.md](CHANGELOG.md)

## Development

Run example:

```
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
